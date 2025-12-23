package me.miki.shindo.management.network.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.ShindoAPI;
import me.miki.shindo.api.websocket.ShindoWebsocket;
import me.miki.shindo.api.websocket.message.MessageType;
import me.miki.shindo.logger.ShindoLogger;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * WarpProxyManager integrates Cloudflare's 1.1.1.1 (WARP) stack inside the client. It handles session/bootstrap
 * diagnostics, DNS-over-HTTPS lookups, caching and telemetry that feeds both the GUI and the WebSocket layer.
 */
public class WarpProxyManager {

    private static final String TRACE_ENDPOINT = "https://1.1.1.1/cdn-cgi/trace";
    private static final String CLOUDFLARE_ENDPOINT = "https://cloudflare-dns.com/dns-query";
    private static final String[] WARMUP_HOSTS = {"api.minecraftservices.com", "mojang.com", "minecraft.net"};
    private static final int DEFAULT_TIMEOUT_MS = 2500;
    private static final int MIN_TTL_SECONDS = 30;
    private static final int MAX_CNAME_CHAIN = 4;
    private static final int MAX_CACHE_ENTRIES = 128;
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(?:\\d{1,3}\\.){3}\\d{1,3}$");
    private static final Pattern IPV6_PATTERN = Pattern.compile("^\\[?[0-9a-fA-F:]+\\]?$");
    private static final long BROADCAST_DEBOUNCE_MS = 1500L;

    private final Gson gson = new Gson();
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ExecutorService worker;
    private final ScheduledExecutorService scheduler;
    private final AtomicReference<WarpSession> sessionRef = new AtomicReference<>(WarpSession.disabled());
    private final AtomicReference<WarpDiagnostics> diagnosticsRef = new AtomicReference<>(WarpDiagnostics.disabled());
    private final AtomicLong lastBroadcastAt = new AtomicLong(0L);

    @Getter
    private volatile boolean enabled;

    public WarpProxyManager() {
        worker = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Shindo-WarpWorker");
            thread.setDaemon(true);
            return thread;
        });
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Shindo-WarpHealth");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::runHealthProbe, 10L, 45L, TimeUnit.SECONDS);
    }

    public synchronized void setEnabled(boolean enabled) {
        boolean previous = this.enabled;
        this.enabled = enabled;
        if (!enabled) {
            cache.clear();
            sessionRef.set(WarpSession.disabled());
            updateDiagnostics(WarpStatus.DISABLED, null, 0L, false, System.currentTimeMillis(), null);
            return;
        }
        if (!previous) {
            updateDiagnostics(WarpStatus.RESOLVING, null, 0L, false, System.currentTimeMillis(), null);
            worker.execute(this::bootstrapSession);
        }
    }

    public WarpDiagnostics getDiagnostics() {
        return diagnosticsRef.get();
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

        try {
            LookupOutcome outcome = performLookup(target);
            if (!outcome.viaFallback) {
                cache.put(target, new CacheEntry(cloneAddresses(outcome.addresses),
                        outcome.lookupDurationMs,
                        outcome.resolvedAt,
                        computeExpiry(outcome.resolvedAt, outcome.ttlSeconds)));
                trimCacheIfNeeded();
            } else {
                cache.remove(target);
            }
            WarpStatus status = outcome.viaFallback ? WarpStatus.BYPASSED : WarpStatus.ACTIVE;
            updateDiagnostics(status, firstAddress(outcome.addresses), outcome.lookupDurationMs, false, outcome.resolvedAt, null);
            return outcome.addresses;
        } catch (IOException exception) {
            updateDiagnostics(WarpStatus.ERROR, null, 0L, false, System.currentTimeMillis(), exception.getMessage());
            throw exception;
        }
    }

    private void bootstrapSession() {
        if (!enabled) {
            return;
        }
        try {
            WarpSession session = probeWarpSession();
            if (!enabled) {
                return;
            }
            sessionRef.set(session);
            updateDiagnostics(WarpStatus.IDLE, null, 0L, false, System.currentTimeMillis(), null);
            warmupDns();
        } catch (IOException exception) {
            ShindoLogger.error("Failed to bootstrap Cloudflare WARP session", exception);
            updateDiagnostics(WarpStatus.ERROR, null, 0L, false, System.currentTimeMillis(), exception.getMessage());
        }
    }

    private void warmupDns() {
        for (String host : WARMUP_HOSTS) {
            if (!enabled) {
                return;
            }
            try {
                LookupOutcome outcome = performLookup(host);
                WarpStatus status = outcome.viaFallback ? WarpStatus.BYPASSED : WarpStatus.ACTIVE;
                updateDiagnostics(status, firstAddress(outcome.addresses), outcome.lookupDurationMs, false, outcome.resolvedAt, null);
                return;
            } catch (IOException exception) {
                updateDiagnostics(WarpStatus.ERROR, null, 0L, false, System.currentTimeMillis(), exception.getMessage());
            }
        }
    }

    private void runHealthProbe() {
        if (!enabled) {
            return;
        }
        try {
            WarpSession session = probeWarpSession();
            sessionRef.set(session);
            refreshDiagnosticsMeta(session);
        } catch (IOException ignored) {
        }
    }

    private void refreshDiagnosticsMeta(WarpSession session) {
        WarpDiagnostics current = diagnosticsRef.get();
        if (current == null) {
            return;
        }
        WarpDiagnostics refreshed = new WarpDiagnostics(
                enabled,
                current.getStatus(),
                current.getLastResolver(),
                current.getLastLookupDurationMs(),
                current.getLastUpdatedAt(),
                current.getLastError(),
                current.isCacheHit(),
                session.mode,
                session.lastLatencyMs,
                session.establishedAt);
        publishDiagnostics(refreshed);
    }

    private LookupOutcome performLookup(String host) throws IOException {
        long start = System.nanoTime();
        LookupPayload payload = null;
        IOException warpError = null;
        try {
            payload = queryCloudflare(host);
        } catch (IOException exception) {
            warpError = exception;
        }

        InetAddress[] addresses = null;
        boolean fallback = false;
        int ttlSeconds = MIN_TTL_SECONDS;
        if (payload != null && payload.addresses.length > 0) {
            addresses = payload.addresses;
            ttlSeconds = Math.max(MIN_TTL_SECONDS, payload.ttlSeconds);
        } else {
            if (warpError != null) {
                ShindoLogger.warn("Warp DNS lookup failed for host " + host + ": " + warpError.getMessage());
            }
            addresses = InetAddress.getAllByName(host);
            fallback = true;
        }

        long resolvedAt = System.currentTimeMillis();
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        return new LookupOutcome(addresses, ttlSeconds, fallback, duration, resolvedAt);
    }

    private LookupPayload queryCloudflare(String host) throws IOException {
        return queryCloudflareInternal(host, 0, new ArrayList<String>());
    }

    private LookupPayload queryCloudflareInternal(String host, int depth, List<String> visited) throws IOException {
        if (depth > MAX_CNAME_CHAIN) {
            return LookupPayload.empty();
        }
        String normalised = normaliseHost(host);
        if (normalised.isEmpty() || visited.contains(normalised)) {
            return LookupPayload.empty();
        }
        visited.add(normalised);

        List<InetAddress> ipv4 = new ArrayList<>();
        List<Integer> ttl4 = new ArrayList<>();
        List<InetAddress> ipv6 = new ArrayList<>();
        List<Integer> ttl6 = new ArrayList<>();
        List<String> aliases = new ArrayList<>();

        queryRecord(normalised, "A", ipv4, ttl4, aliases);
        if (ipv4.isEmpty()) {
            queryRecord(normalised, "AAAA", ipv6, ttl6, aliases);
        }

        if (!ipv4.isEmpty()) {
            return new LookupPayload(ipv4.toArray(new InetAddress[0]), minimumTtl(ttl4));
        }
        if (!ipv6.isEmpty()) {
            return new LookupPayload(ipv6.toArray(new InetAddress[0]), minimumTtl(ttl6));
        }

        for (String alias : aliases) {
            LookupPayload payload = queryCloudflareInternal(alias, depth + 1, visited);
            if (payload.addresses.length > 0) {
                return payload;
            }
        }
        return LookupPayload.empty();
    }

    private void queryRecord(String host,
                             String type,
                             List<InetAddress> out,
                             List<Integer> ttlCollector,
                             List<String> aliases) throws IOException {
        HttpsURLConnection connection = null;
        try {
            String encodedHost = URLEncoder.encode(host, StandardCharsets.UTF_8.name());
            URL url = new URL(CLOUDFLARE_ENDPOINT + "?name=" + encodedHost + "&type=" + type + "&ct=application/dns-json");
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

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                if (json == null) {
                    throw new IOException("Empty response from Cloudflare DNS");
                }

                int statusCode = json.has("Status") ? json.get("Status").getAsInt() : -1;
                if (statusCode != 0) {
                    throw new IOException("Cloudflare DNS query failed with status " + statusCode);
                }

                JsonArray answers = json.has("Answer") && json.get("Answer").isJsonArray()
                        ? json.getAsJsonArray("Answer")
                        : new JsonArray();
                for (JsonElement element : answers) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    JsonObject answer = element.getAsJsonObject();
                    int recordType = answer.has("type") ? answer.get("type").getAsInt() : -1;
                    if (recordType == 5) {
                        String alias = answer.has("data") ? answer.get("data").getAsString() : null;
                        if (alias != null) {
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
                    } catch (Exception ignored) {
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

    private WarpSession probeWarpSession() throws IOException {
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(TRACE_ENDPOINT);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
            connection.setReadTimeout(DEFAULT_TIMEOUT_MS);
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "ShindoClient/5 WarpProbe");
            long start = System.nanoTime();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                Map<String, String> values = new HashMap<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    int idx = line.indexOf('=');
                    if (idx <= 0 || idx >= line.length() - 1) {
                        continue;
                    }
                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1).trim();
                    values.put(key, value);
                }
                long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                String warpMode = values.getOrDefault("warp", "off");
                String ip = values.getOrDefault("ip", "");
                return new WarpSession(warpMode, ip, System.currentTimeMillis(), latencyMs);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void updateDiagnostics(WarpStatus status,
                                   String resolver,
                                   long lookupDurationMs,
                                   boolean cacheHit,
                                   long updatedAt,
                                   String error) {
        WarpSession session = sessionRef.get();
        WarpDiagnostics diagnostics = new WarpDiagnostics(
                enabled,
                status,
                resolver,
                lookupDurationMs,
                updatedAt,
                error,
                cacheHit,
                session.mode,
                session.lastLatencyMs,
                session.establishedAt);
        publishDiagnostics(diagnostics);
    }

    private void publishDiagnostics(WarpDiagnostics diagnostics) {
        diagnosticsRef.set(diagnostics);
        long now = System.currentTimeMillis();
        long last = lastBroadcastAt.get();
        if (diagnostics.getStatus() != WarpStatus.ERROR && now - last < BROADCAST_DEBOUNCE_MS) {
            return;
        }
        lastBroadcastAt.set(now);
        pushDiagnosticsToWs(diagnostics);
    }

    private void pushDiagnosticsToWs(WarpDiagnostics diagnostics) {
        try {
            ShindoAPI api = Shindo.getInstance().getShindoAPI();
            if (api == null) {
                return;
            }
            ShindoWebsocket ws = api.getWs();
            if (ws == null) {
                return;
            }
            JsonObject payload = new JsonObject();
            payload.addProperty("enabled", diagnostics.isEnabled());
            payload.addProperty("status", diagnostics.getStatus().name());
            if (diagnostics.getLastResolver() != null) {
                payload.addProperty("resolver", diagnostics.getLastResolver());
            }
            payload.addProperty("lookupMs", diagnostics.getLastLookupDurationMs());
            payload.addProperty("timestamp", diagnostics.getLastUpdatedAt());
            payload.addProperty("cacheHit", diagnostics.isCacheHit());
            if (diagnostics.getLastError() != null) {
                payload.addProperty("error", diagnostics.getLastError());
            }
            payload.addProperty("warpMode", diagnostics.getWarpMode());
            payload.addProperty("warpLatency", diagnostics.getLastHealthLatencyMs());
            payload.addProperty("sessionStartedAt", diagnostics.getSessionEstablishedAt());
            ws.send(MessageType.WARP_STATUS, payload);
        } catch (Exception ignored) {
        }
    }

    private void trimCacheIfNeeded() {
        int size = cache.size();
        if (size <= MAX_CACHE_ENTRIES) {
            return;
        }
        int removeBudget = size - MAX_CACHE_ENTRIES;
        Iterator<String> iterator = cache.keySet().iterator();
        while (removeBudget > 0 && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            removeBudget--;
        }
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
            if (value != null) {
                min = Math.min(min, value);
            }
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

    private static final class LookupPayload {
        private final InetAddress[] addresses;
        private final int ttlSeconds;

        private LookupPayload(InetAddress[] addresses, int ttlSeconds) {
            this.addresses = addresses != null ? addresses : new InetAddress[0];
            this.ttlSeconds = ttlSeconds;
        }

        private static LookupPayload empty() {
            return new LookupPayload(new InetAddress[0], MIN_TTL_SECONDS);
        }
    }

    private static final class LookupOutcome {
        private final InetAddress[] addresses;
        private final int ttlSeconds;
        private final boolean viaFallback;
        private final long lookupDurationMs;
        private final long resolvedAt;

        private LookupOutcome(InetAddress[] addresses,
                              int ttlSeconds,
                              boolean viaFallback,
                              long lookupDurationMs,
                              long resolvedAt) {
            this.addresses = addresses != null ? addresses : new InetAddress[0];
            this.ttlSeconds = ttlSeconds;
            this.viaFallback = viaFallback;
            this.lookupDurationMs = lookupDurationMs;
            this.resolvedAt = resolvedAt;
        }
    }

    private static final class WarpSession {
        private final String mode;
        private final String publicIp;
        private final long establishedAt;
        private final long lastLatencyMs;

        private WarpSession(String mode, String publicIp, long establishedAt, long lastLatencyMs) {
            this.mode = mode;
            this.publicIp = publicIp;
            this.establishedAt = establishedAt;
            this.lastLatencyMs = lastLatencyMs;
        }

        private static WarpSession disabled() {
            return new WarpSession("off", "", 0L, 0L);
        }
    }

    public static final class WarpDiagnostics {
        private final boolean enabled;
        private final WarpStatus status;
        private final String lastResolver;
        private final long lastLookupDurationMs;
        private final long lastUpdatedAt;
        private final String lastError;
        private final boolean cacheHit;
        private final String warpMode;
        private final long lastHealthLatencyMs;
        private final long sessionEstablishedAt;

        private WarpDiagnostics(boolean enabled, WarpStatus status, String lastResolver, long lastLookupDurationMs, long lastUpdatedAt, String lastError, boolean cacheHit, String warpMode, long lastHealthLatencyMs, long sessionEstablishedAt) {
            this.enabled = enabled;
            this.status = status;
            this.lastResolver = lastResolver;
            this.lastLookupDurationMs = lastLookupDurationMs;
            this.lastUpdatedAt = lastUpdatedAt;
            this.lastError = lastError;
            this.cacheHit = cacheHit;
            this.warpMode = warpMode;
            this.lastHealthLatencyMs = lastHealthLatencyMs;
            this.sessionEstablishedAt = sessionEstablishedAt;
        }

        private static WarpDiagnostics disabled() {
            return new WarpDiagnostics(false, WarpStatus.DISABLED, null, 0L, 0L, null, false, "off", 0L, 0L);
        }

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

        public String getWarpMode() {
            return warpMode;
        }

        public long getLastHealthLatencyMs() {
            return lastHealthLatencyMs;
        }

        public long getSessionEstablishedAt() {
            return sessionEstablishedAt;
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
