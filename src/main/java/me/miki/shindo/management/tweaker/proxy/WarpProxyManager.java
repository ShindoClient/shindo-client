package me.miki.shindo.management.tweaker.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.miki.shindo.logger.ShindoLogger;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Provides a lightweight Cloudflare WARP inspired DNS proxy. The manager performs DNS-over-HTTPS lookups against
 * Cloudflare's 1.1.1.1 resolver, caches responses and exposes diagnostics that can be surfaced inside the mod menu.
 * It intentionally keeps the API simple: resolve a host when enabled, fall back to the JVM resolver otherwise.
 */
public class WarpProxyManager {

    private static final String CLOUDFLARE_ENDPOINT = "https://cloudflare-dns.com/dns-query";
    private static final int DEFAULT_TIMEOUT_MS = 2500;
    private static final int MIN_TTL_SECONDS = 30;
    private static final int MAX_CNAME_CHAIN = 4;
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(?:\\d{1,3}\\.){3}\\d{1,3}$");
    private static final Pattern IPV6_PATTERN = Pattern.compile("^\\[?[0-9a-fA-F:]+\\]?$");

    private final Gson gson = new Gson();
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Getter
    private volatile boolean enabled;
    private volatile WarpStatus status = WarpStatus.DISABLED;
    private volatile long lastLookupDurationMs;
    private volatile long lastUpdatedAt;
    private volatile String lastResolver;
    private volatile String lastError;
    private volatile boolean lastCacheHit;

    public WarpProxyManager() {
        this.enabled = false;
        resetDiagnostics(false);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            cache.clear();
            resetDiagnostics(false);
            status = WarpStatus.DISABLED;
            return;
        }
        resetDiagnostics(true);
        status = WarpStatus.IDLE;
    }

    public WarpDiagnostics getDiagnostics() {
        WarpDiagnostics diagnostics = new WarpDiagnostics();
        diagnostics.enabled = enabled;
        diagnostics.status = status;
        diagnostics.lastResolver = lastResolver;
        diagnostics.lastLookupDurationMs = lastLookupDurationMs;
        diagnostics.lastUpdatedAt = lastUpdatedAt;
        diagnostics.lastError = lastError;
        diagnostics.cacheHit = lastCacheHit;
        return diagnostics;
    }

    public InetSocketAddress resolveEndpoint(String host, int port) throws IOException {
        InetAddress[] addresses = resolve(host);
        if (addresses == null || addresses.length == 0 || addresses[0] == null) {
            return null;
        }
        return new InetSocketAddress(addresses[0], port);
    }

    public InetAddress[] resolve(String host) throws IOException {
        if (!enabled) {
            status = WarpStatus.DISABLED;
            return null;
        }

        String target = normaliseHost(host);
        if (target.isEmpty()) {
            return null;
        }

        if (isIpLiteral(target)) {
            InetAddress address = InetAddress.getByName(target.replace("[", "").replace("]", ""));
            updateDiagnostics(WarpStatus.BYPASSED, target, 0L, true, System.currentTimeMillis(), null);
            return new InetAddress[]{address};
        }

        CacheEntry cached = cache.get(target);
        long now = System.currentTimeMillis();
        if (cached != null && cached.expiryAt > now) {
            updateDiagnostics(WarpStatus.CACHED, firstAddress(cached.addresses), cached.lookupDurationMs, true, cached.resolvedAt, null);
            return cloneAddresses(cached.addresses);
        }

        status = WarpStatus.RESOLVING;
        lastCacheHit = false;
        lastError = null;

        long start = System.nanoTime();
        try {
            LookupResponse response = queryCloudflare(target);
            InetAddress[] resolved = response.addresses;
            if (resolved == null || resolved.length == 0) {
                resolved = InetAddress.getAllByName(target);
            }
            long resolvedAt = System.currentTimeMillis();
            long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            long expiryAt = computeExpiry(resolvedAt, response.ttlSeconds);
            if (resolved != null && resolved.length > 0) {
                cache.put(target, new CacheEntry(cloneAddresses(resolved), duration, resolvedAt, expiryAt));
            }
            updateDiagnostics(WarpStatus.ACTIVE, firstAddress(resolved), duration, false, resolvedAt, null);
            return resolved;
        } catch (IOException exception) {
            updateDiagnostics(WarpStatus.ERROR, null, 0L, false, now, exception.getMessage());
            throw exception;
        }
    }

    private LookupResponse queryCloudflare(String host) throws IOException {
        return queryCloudflareInternal(host, 0, new ArrayList<String>());
    }

    private LookupResponse queryCloudflareInternal(String host, int depth, List<String> visited) throws IOException {
        if (depth > MAX_CNAME_CHAIN) {
            return new LookupResponse(InetAddress.getAllByName(host), MIN_TTL_SECONDS);
        }

        String normalisedHost = normaliseHost(host);
        if (visited.contains(normalisedHost)) {
            return new LookupResponse(InetAddress.getAllByName(normalisedHost), MIN_TTL_SECONDS);
        }
        visited.add(normalisedHost);

        List<String> aliases = new ArrayList<>();
        List<InetAddress> addresses = new ArrayList<>();
        List<Integer> ttlValues = new ArrayList<>();
        List<InetAddress> ipv6Addresses = new ArrayList<>();
        List<Integer> ttlValuesV6 = new ArrayList<>();

        queryRecord(normalisedHost, "A", addresses, ttlValues, aliases);
        if (addresses.isEmpty()) {
            queryRecord(normalisedHost, "AAAA", ipv6Addresses, ttlValuesV6, aliases);
        }

        if (!addresses.isEmpty()) {
            return new LookupResponse(addresses.toArray(new InetAddress[0]), minimumTtl(ttlValues));
        }
        if (!ipv6Addresses.isEmpty()) {
            return new LookupResponse(ipv6Addresses.toArray(new InetAddress[0]), minimumTtl(ttlValuesV6));
        }

        for (String alias : aliases) {
            LookupResponse response = queryCloudflareInternal(alias, depth + 1, visited);
            if (response != null && response.addresses.length > 0) {
                return response;
            }
        }

        return new LookupResponse(InetAddress.getAllByName(normalisedHost), MIN_TTL_SECONDS);
    }

    private void queryRecord(String host, String type, List<InetAddress> out, List<Integer> ttlCollector, List<String> aliases) throws IOException {
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(CLOUDFLARE_ENDPOINT + "?name=" + host + "&type=" + type);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
            connection.setReadTimeout(DEFAULT_TIMEOUT_MS);
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/dns-json");
            connection.setRequestProperty("User-Agent", "ShindoClient/5 WarpResolver");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Cloudflare DNS returned HTTP " + responseCode);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                if (json == null) {
                    throw new IOException("Empty response from Cloudflare DNS");
                }

                int statusCode = json.has("Status") ? json.get("Status").getAsInt() : -1;
                if (statusCode != 0) {
                    throw new IOException("Cloudflare DNS query failed with status " + statusCode);
                }

                JsonArray answers = json.has("Answer") && json.get("Answer").isJsonArray() ? json.getAsJsonArray("Answer") : new JsonArray();
                for (JsonElement element : answers) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    JsonObject answer = element.getAsJsonObject();
                    int recordType = answer.has("type") ? answer.get("type").getAsInt() : -1;
                    if (recordType == 5) { // CNAME
                        String alias = answer.has("data") ? answer.get("data").getAsString() : null;
                        if (alias != null && !alias.isEmpty()) {
                            String aliasHost = normaliseHost(alias);
                            if (!aliasHost.isEmpty()) {
                                aliases.add(aliasHost);
                            }
                        }
                        continue;
                    }
                    if (!matchesType(type, recordType)) {
                        continue;
                    }
                    String data = answer.has("data") ? answer.get("data").getAsString() : null;
                    if (data == null || data.isEmpty()) {
                        continue;
                    }
                    try {
                        out.add(InetAddress.getByName(data));
                        int ttl = answer.has("TTL") ? answer.get("TTL").getAsInt() : MIN_TTL_SECONDS;
                        ttlCollector.add(ttl);
                    } catch (Exception e) {
                        ShindoLogger.error("Failed to parse DNS answer '" + data + "' for host " + host, e);
                    }
                }
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean matchesType(String type, int recordType) {
        if ("A".equalsIgnoreCase(type)) {
            return recordType == 1;
        }
        if ("AAAA".equalsIgnoreCase(type)) {
            return recordType == 28;
        }
        return false;
    }

    private void updateDiagnostics(WarpStatus status, String resolver, long lookupDurationMs, boolean cacheHit, long updatedAt, String error) {
        this.status = status;
        this.lastResolver = resolver;
        this.lastLookupDurationMs = lookupDurationMs;
        this.lastCacheHit = cacheHit;
        this.lastUpdatedAt = updatedAt;
        this.lastError = error;
    }

    private void resetDiagnostics(boolean preserveStatus) {
        if (!preserveStatus) {
            status = WarpStatus.DISABLED;
        }
        lastResolver = null;
        lastLookupDurationMs = 0L;
        lastCacheHit = false;
        lastUpdatedAt = 0L;
        lastError = null;
    }

    private String normaliseHost(String host) {
        if (host == null) {
            return "";
        }
        String cleaned = host.trim().toLowerCase();
        while (cleaned.endsWith(".")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned;
    }

    private boolean isIpLiteral(String host) {
        return IPV4_PATTERN.matcher(host).matches() || IPV6_PATTERN.matcher(host).matches();
    }

    private InetAddress[] cloneAddresses(InetAddress[] addresses) {
        if (addresses == null) {
            return new InetAddress[0];
        }
        InetAddress[] clone = new InetAddress[addresses.length];
        System.arraycopy(addresses, 0, clone, 0, addresses.length);
        return clone;
    }

    private String firstAddress(InetAddress[] addresses) {
        if (addresses == null || addresses.length == 0 || addresses[0] == null) {
            return null;
        }
        return addresses[0].getHostAddress();
    }

    private long computeExpiry(long resolvedAt, int ttlSeconds) {
        int ttl = Math.max(MIN_TTL_SECONDS, ttlSeconds);
        return resolvedAt + TimeUnit.SECONDS.toMillis(ttl);
    }

    private int minimumTtl(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return MIN_TTL_SECONDS;
        }
        int min = Integer.MAX_VALUE;
        for (Integer value : values) {
            if (value == null) {
                continue;
            }
            min = Math.min(min, value);
        }
        if (min == Integer.MAX_VALUE || min <= 0) {
            return MIN_TTL_SECONDS;
        }
        return Math.max(MIN_TTL_SECONDS, min);
    }

    private static final class CacheEntry {
        private final InetAddress[] addresses;
        private final long lookupDurationMs;
        private final long resolvedAt;
        private final long expiryAt;

        private CacheEntry(InetAddress[] addresses, long lookupDurationMs, long resolvedAt, long expiryAt) {
            this.addresses = addresses != null ? addresses : new InetAddress[0];
            this.lookupDurationMs = lookupDurationMs;
            this.resolvedAt = resolvedAt;
            this.expiryAt = expiryAt;
        }
    }

    private static final class LookupResponse {
        private final InetAddress[] addresses;
        private final int ttlSeconds;

        private LookupResponse(InetAddress[] addresses, int ttlSeconds) {
            this.addresses = addresses != null ? addresses : new InetAddress[0];
            this.ttlSeconds = ttlSeconds;
        }
    }

    public static class WarpDiagnostics {
        private boolean enabled;
        private WarpStatus status;
        private String lastResolver;
        private long lastLookupDurationMs;
        private long lastUpdatedAt;
        private String lastError;
        private boolean cacheHit;

        public boolean isEnabled() {
            return enabled;
        }

        public WarpStatus getStatus() {
            return status;
        }

        public String getLastResolver() {
            return lastResolver;
        }

        public long getLastLookupDurationMs() {
            return lastLookupDurationMs;
        }

        public long getLastUpdatedAt() {
            return lastUpdatedAt;
        }

        public String getLastError() {
            return lastError;
        }

        public boolean isCacheHit() {
            return cacheHit;
        }
    }

    public enum WarpStatus {
        DISABLED,
        IDLE,
        RESOLVING,
        ACTIVE,
        CACHED,
        BYPASSED,
        ERROR
    }
}
