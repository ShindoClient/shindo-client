package me.miki.shindo.management.network.proxy

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.ShindoAPI
import me.miki.shindo.api.websocket.ShindoWebsocket
import me.miki.shindo.api.websocket.message.MessageType
import me.miki.shindo.logger.ShindoLogger
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection

/**
 * WarpProxyManager integra a stack Cloudflare 1.1.1.1 (WARP) no cliente.
 * Gerencia diagnóstico de sessão/bootstrap, lookups DNS-over-HTTPS, cache e telemetria
 * que alimenta tanto a GUI quanto a camada WebSocket.
 */
class WarpProxyManager {

    companion object {
        private const val TRACE_ENDPOINT = "https://1.1.1.1/cdn-cgi/trace"
        private const val CLOUDFLARE_ENDPOINT = "https://cloudflare-dns.com/dns-query"
        private val WARMUP_HOSTS = arrayOf("api.minecraftservices.com", "mojang.com", "minecraft.net")
        private const val DEFAULT_TIMEOUT_MS = 2500
        private const val MIN_TTL_SECONDS = 30
        private const val MAX_CNAME_CHAIN = 4
        private const val MAX_CACHE_ENTRIES = 128
        private val IPV4_PATTERN = Pattern.compile("^(?:\\d{1,3}\\.){3}\\d{1,3}$")
        private val IPV6_PATTERN = Pattern.compile("^\\[?[0-9a-fA-F:]+\\]?$")
        private const val BROADCAST_DEBOUNCE_MS = 1500L
    }

    private val gson = Gson()
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val worker: ExecutorService
    private val scheduler: ScheduledExecutorService
    private val sessionRef = AtomicReference<WarpSession>(WarpSession.disabled())
    private val diagnosticsRef = AtomicReference<WarpDiagnostics>(WarpDiagnostics.disabled())
    private val lastBroadcastAt = AtomicLong(0L)

    @Volatile
    private var enabled = false

    init {
        worker = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "Shindo-WarpWorker").apply {
                isDaemon = true
            }
        }
        scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "Shindo-WarpHealth").apply {
                isDaemon = true
            }
        }
        scheduler.scheduleWithFixedDelay({ runHealthProbe() }, 10L, 45L, TimeUnit.SECONDS)
    }

    @Synchronized
    fun setEnabled(enabled: Boolean) {
        val previous = this.enabled
        this.enabled = enabled
        if (!enabled) {
            cache.clear()
            sessionRef.set(WarpSession.disabled())
            updateDiagnostics(WarpStatus.DISABLED, null, 0L, false, System.currentTimeMillis(), null)
            return
        }
        if (!previous) {
            updateDiagnostics(WarpStatus.RESOLVING, null, 0L, false, System.currentTimeMillis(), null)
            worker.execute { bootstrapSession() }
        }
    }

    fun isEnabled(): Boolean = enabled

    fun getEnabled(): Boolean = enabled

    fun getDiagnostics(): WarpDiagnostics = diagnosticsRef.get()

    @Throws(IOException::class)
    fun resolveEndpoint(host: String, port: Int): InetSocketAddress? {
        val addresses = resolve(host)
        if (addresses == null || addresses.isEmpty() || addresses[0] == null) {
            return null
        }
        return InetSocketAddress(addresses[0], port)
    }

    @Throws(IOException::class)
    fun resolve(host: String): Array<InetAddress>? {
        if (!enabled) {
            return null
        }

        val target = normaliseHost(host)
        if (target.isEmpty()) {
            return null
        }

        if (isIpLiteral(target)) {
            val address = InetAddress.getByName(target.replace("[", "").replace("]", ""))
            updateDiagnostics(WarpStatus.BYPASSED, target, 0L, true, System.currentTimeMillis(), null)
            return arrayOf(address)
        }

        val cached = cache[target]
        val now = System.currentTimeMillis()
        if (cached != null && cached.expiryAt > now) {
            updateDiagnostics(
                WarpStatus.CACHED,
                firstAddress(cached.addresses),
                cached.lookupDurationMs,
                true,
                cached.resolvedAt,
                null
            )
            return cloneAddresses(cached.addresses)
        }

        try {
            val outcome = performLookup(target)
            if (!outcome.viaFallback) {
                cache[target] = CacheEntry(
                    cloneAddresses(outcome.addresses),
                    outcome.lookupDurationMs,
                    outcome.resolvedAt,
                    computeExpiry(outcome.resolvedAt, outcome.ttlSeconds)
                )
                trimCacheIfNeeded()
            } else {
                cache.remove(target)
            }
            val status = if (outcome.viaFallback) WarpStatus.BYPASSED else WarpStatus.ACTIVE
            updateDiagnostics(
                status,
                firstAddress(outcome.addresses),
                outcome.lookupDurationMs,
                false,
                outcome.resolvedAt,
                null
            )
            return outcome.addresses
        } catch (exception: IOException) {
            updateDiagnostics(
                WarpStatus.ERROR,
                null,
                0L,
                false,
                System.currentTimeMillis(),
                exception.message
            )
            throw exception
        }
    }

    private fun bootstrapSession() {
        if (!enabled) return
        try {
            val session = probeWarpSession()
            if (!enabled) return
            sessionRef.set(session)
            updateDiagnostics(WarpStatus.IDLE, null, 0L, false, System.currentTimeMillis(), null)
            warmupDns()
        } catch (exception: IOException) {
            ShindoLogger.error("Failed to bootstrap Cloudflare WARP session", exception)
            updateDiagnostics(
                WarpStatus.ERROR,
                null,
                0L,
                false,
                System.currentTimeMillis(),
                exception.message
            )
        }
    }

    private fun warmupDns() {
        for (host in WARMUP_HOSTS) {
            if (!enabled) return
            try {
                val outcome = performLookup(host)
                val status = if (outcome.viaFallback) WarpStatus.BYPASSED else WarpStatus.ACTIVE
                updateDiagnostics(
                    status,
                    firstAddress(outcome.addresses),
                    outcome.lookupDurationMs,
                    false,
                    outcome.resolvedAt,
                    null
                )
                return
            } catch (exception: IOException) {
                updateDiagnostics(
                    WarpStatus.ERROR,
                    null,
                    0L,
                    false,
                    System.currentTimeMillis(),
                    exception.message
                )
            }
        }
    }

    private fun runHealthProbe() {
        if (!enabled) return
        try {
            val session = probeWarpSession()
            sessionRef.set(session)
            refreshDiagnosticsMeta(session)
        } catch (ignored: IOException) {
        }
    }

    private fun refreshDiagnosticsMeta(session: WarpSession) {
        val current = diagnosticsRef.get() ?: return
        val refreshed = WarpDiagnostics(
            enabled,
            current.status,
            current.lastResolver,
            current.lastLookupDurationMs,
            current.lastUpdatedAt,
            current.lastError,
            current.cacheHit,
            session.mode,
            session.lastLatencyMs,
            session.establishedAt
        )
        publishDiagnostics(refreshed)
    }

    @Throws(IOException::class)
    private fun performLookup(host: String): LookupOutcome {
        val start = System.nanoTime()
        var payload: LookupPayload? = null
        var warpError: IOException? = null
        try {
            payload = queryCloudflare(host)
        } catch (exception: IOException) {
            warpError = exception
        }

        val addresses: Array<InetAddress>
        var fallback = false
        var ttlSeconds = MIN_TTL_SECONDS
        if (payload != null && payload.addresses.isNotEmpty()) {
            addresses = payload.addresses
            ttlSeconds = maxOf(MIN_TTL_SECONDS, payload.ttlSeconds)
        } else {
            if (warpError != null) {
                ShindoLogger.warn("Warp DNS lookup failed for host $host: ${warpError.message}")
            }
            addresses = InetAddress.getAllByName(host)
            fallback = true
        }

        val resolvedAt = System.currentTimeMillis()
        val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
        return LookupOutcome(addresses, ttlSeconds, fallback, duration, resolvedAt)
    }

    @Throws(IOException::class)
    private fun queryCloudflare(host: String): LookupPayload {
        return queryCloudflareInternal(host, 0, mutableListOf())
    }

    @Throws(IOException::class)
    private fun queryCloudflareInternal(host: String, depth: Int, visited: MutableList<String>): LookupPayload {
        if (depth > MAX_CNAME_CHAIN) {
            return LookupPayload.empty()
        }
        val normalised = normaliseHost(host)
        if (normalised.isEmpty() || visited.contains(normalised)) {
            return LookupPayload.empty()
        }
        visited.add(normalised)

        val ipv4 = mutableListOf<InetAddress>()
        val ttl4 = mutableListOf<Int>()
        val ipv6 = mutableListOf<InetAddress>()
        val ttl6 = mutableListOf<Int>()
        val aliases = mutableListOf<String>()

        queryRecord(normalised, "A", ipv4, ttl4, aliases)
        if (ipv4.isEmpty()) {
            queryRecord(normalised, "AAAA", ipv6, ttl6, aliases)
        }

        if (ipv4.isNotEmpty()) {
            return LookupPayload(ipv4.toTypedArray(), minimumTtl(ttl4))
        }
        if (ipv6.isNotEmpty()) {
            return LookupPayload(ipv6.toTypedArray(), minimumTtl(ttl6))
        }

        for (alias in aliases) {
            val payload = queryCloudflareInternal(alias, depth + 1, visited)
            if (payload.addresses.isNotEmpty()) {
                return payload
            }
        }
        return LookupPayload.empty()
    }

    @Throws(IOException::class)
    private fun queryRecord(
        host: String,
        type: String,
        out: MutableList<InetAddress>,
        ttlCollector: MutableList<Int>,
        aliases: MutableList<String>
    ) {
        var connection: HttpsURLConnection? = null
        try {
            val encodedHost = URLEncoder.encode(host, StandardCharsets.UTF_8.name())
            val url = URL("$CLOUDFLARE_ENDPOINT?name=$encodedHost&type=$type&ct=application/dns-json")
            connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = DEFAULT_TIMEOUT_MS
            connection.readTimeout = DEFAULT_TIMEOUT_MS
            connection.useCaches = false
            connection.setRequestProperty("Accept", "application/dns-json")
            connection.setRequestProperty("User-Agent", "ShindoClient/5 WarpResolver")

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                throw IOException("Cloudflare DNS returned HTTP $responseCode")
            }

            BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)).use { reader ->
                val json = gson.fromJson(reader, JsonObject::class.java)
                    ?: throw IOException("Empty response from Cloudflare DNS")

                val statusCode = if (json.has("Status")) json.get("Status").asInt else -1
                if (statusCode != 0) {
                    throw IOException("Cloudflare DNS query failed with status $statusCode")
                }

                val answers = if (json.has("Answer") && json.get("Answer").isJsonArray) {
                    json.getAsJsonArray("Answer")
                } else {
                    JsonArray()
                }

                for (element in answers) {
                    if (!element.isJsonObject) continue
                    val answer = element.asJsonObject
                    val recordType = if (answer.has("type")) answer.get("type").asInt else -1
                    if (recordType == 5) {
                        val alias = if (answer.has("data")) answer.get("data").asString else null
                        if (alias != null) {
                            val aliasHost = normaliseHost(alias)
                            if (aliasHost.isNotEmpty()) {
                                aliases.add(aliasHost)
                            }
                        }
                        continue
                    }
                    if (!matchesType(type, recordType)) continue
                    val data = if (answer.has("data")) answer.get("data").asString else null
                    if (data.isNullOrEmpty()) continue
                    try {
                        out.add(InetAddress.getByName(data))
                        val ttl = if (answer.has("TTL")) answer.get("TTL").asInt else MIN_TTL_SECONDS
                        ttlCollector.add(ttl)
                    } catch (ignored: Exception) {
                    }
                }
            }
        } finally {
            connection?.disconnect()
        }
    }

    private fun matchesType(type: String, recordType: Int): Boolean {
        return when {
            "A".equals(type, ignoreCase = true) -> recordType == 1
            "AAAA".equals(type, ignoreCase = true) -> recordType == 28
            else -> false
        }
    }

    @Throws(IOException::class)
    private fun probeWarpSession(): WarpSession {
        var connection: HttpsURLConnection? = null
        try {
            val url = URL(TRACE_ENDPOINT)
            connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = DEFAULT_TIMEOUT_MS
            connection.readTimeout = DEFAULT_TIMEOUT_MS
            connection.useCaches = false
            connection.setRequestProperty("User-Agent", "ShindoClient/5 WarpProbe")
            val start = System.nanoTime()
            BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)).use { reader ->
                val values = mutableMapOf<String, String>()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val idx = line!!.indexOf('=')
                    if (idx <= 0 || idx >= line!!.length - 1) continue
                    val key = line!!.substring(0, idx).trim()
                    val value = line!!.substring(idx + 1).trim()
                    values[key] = value
                }
                val latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
                val warpMode = values.getOrDefault("warp", "off")
                val ip = values.getOrDefault("ip", "")
                return WarpSession(warpMode, ip, System.currentTimeMillis(), latencyMs)
            }
        } finally {
            connection?.disconnect()
        }
    }

    private fun updateDiagnostics(
        status: WarpStatus,
        resolver: String?,
        lookupDurationMs: Long,
        cacheHit: Boolean,
        updatedAt: Long,
        error: String?
    ) {
        val session = sessionRef.get()
        val diagnostics = WarpDiagnostics(
            enabled,
            status,
            resolver,
            lookupDurationMs,
            updatedAt,
            error,
            cacheHit,
            session.mode,
            session.lastLatencyMs,
            session.establishedAt
        )
        publishDiagnostics(diagnostics)
    }

    private fun publishDiagnostics(diagnostics: WarpDiagnostics) {
        diagnosticsRef.set(diagnostics)
        val now = System.currentTimeMillis()
        val last = lastBroadcastAt.get()
        if (diagnostics.status != WarpStatus.ERROR && now - last < BROADCAST_DEBOUNCE_MS) {
            return
        }
        lastBroadcastAt.set(now)
        pushDiagnosticsToWs(diagnostics)
    }

    private fun pushDiagnosticsToWs(diagnostics: WarpDiagnostics) {
        try {
            val api = Shindo.getInstance().shindoAPI ?: return
            val ws = api.ws ?: return
            val payload = JsonObject()
            payload.addProperty("enabled", diagnostics.enabled)
            payload.addProperty("status", diagnostics.status.name)
            diagnostics.lastResolver?.let { payload.addProperty("resolver", it) }
            payload.addProperty("lookupMs", diagnostics.lastLookupDurationMs)
            payload.addProperty("timestamp", diagnostics.lastUpdatedAt)
            payload.addProperty("cacheHit", diagnostics.cacheHit)
            diagnostics.lastError?.let { payload.addProperty("error", it) }
            payload.addProperty("warpMode", diagnostics.warpMode)
            payload.addProperty("warpLatency", diagnostics.lastHealthLatencyMs)
            payload.addProperty("sessionStartedAt", diagnostics.sessionEstablishedAt)
            ws.send(MessageType.WARP_STATUS, payload)
        } catch (ignored: Exception) {
        }
    }

    private fun trimCacheIfNeeded() {
        val size = cache.size
        if (size <= MAX_CACHE_ENTRIES) return
        var removeBudget = size - MAX_CACHE_ENTRIES
        val iterator = cache.keys.iterator()
        while (removeBudget > 0 && iterator.hasNext()) {
            iterator.next()
            iterator.remove()
            removeBudget--
        }
    }

    private fun normaliseHost(host: String?): String {
        if (host == null) return ""
        var cleaned = host.trim().lowercase()
        while (cleaned.endsWith(".")) {
            cleaned = cleaned.substring(0, cleaned.length - 1)
        }
        return cleaned
    }

    private fun isIpLiteral(host: String): Boolean {
        return IPV4_PATTERN.matcher(host).matches() || IPV6_PATTERN.matcher(host).matches()
    }

    private fun cloneAddresses(addresses: Array<InetAddress>?): Array<InetAddress> {
        if (addresses == null) return emptyArray()
        return addresses.copyOf()
    }

    private fun firstAddress(addresses: Array<InetAddress>?): String? {
        if (addresses == null || addresses.isEmpty() || addresses[0] == null) {
            return null
        }
        return addresses[0].hostAddress
    }

    private fun computeExpiry(resolvedAt: Long, ttlSeconds: Int): Long {
        val ttl = maxOf(MIN_TTL_SECONDS, ttlSeconds)
        return resolvedAt + TimeUnit.SECONDS.toMillis(ttl.toLong())
    }

    private fun minimumTtl(values: List<Int>): Int {
        if (values.isEmpty()) return MIN_TTL_SECONDS
        val min = values.filterNotNull().minOrNull() ?: return MIN_TTL_SECONDS
        return if (min <= 0) MIN_TTL_SECONDS else maxOf(MIN_TTL_SECONDS, min)
    }

    private data class CacheEntry(
        val addresses: Array<InetAddress>,
        val lookupDurationMs: Long,
        val resolvedAt: Long,
        val expiryAt: Long
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as CacheEntry
            if (!addresses.contentEquals(other.addresses)) return false
            if (lookupDurationMs != other.lookupDurationMs) return false
            if (resolvedAt != other.resolvedAt) return false
            if (expiryAt != other.expiryAt) return false
            return true
        }

        override fun hashCode(): Int {
            var result = addresses.contentHashCode()
            result = 31 * result + lookupDurationMs.hashCode()
            result = 31 * result + resolvedAt.hashCode()
            result = 31 * result + expiryAt.hashCode()
            return result
        }
    }

    private data class LookupPayload(
        val addresses: Array<InetAddress>,
        val ttlSeconds: Int
    ) {
        companion object {
            fun empty(): LookupPayload = LookupPayload(emptyArray(), MIN_TTL_SECONDS)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as LookupPayload
            if (!addresses.contentEquals(other.addresses)) return false
            if (ttlSeconds != other.ttlSeconds) return false
            return true
        }

        override fun hashCode(): Int {
            var result = addresses.contentHashCode()
            result = 31 * result + ttlSeconds
            return result
        }
    }

    private data class LookupOutcome(
        val addresses: Array<InetAddress>,
        val ttlSeconds: Int,
        val viaFallback: Boolean,
        val lookupDurationMs: Long,
        val resolvedAt: Long
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as LookupOutcome
            if (!addresses.contentEquals(other.addresses)) return false
            if (ttlSeconds != other.ttlSeconds) return false
            if (viaFallback != other.viaFallback) return false
            if (lookupDurationMs != other.lookupDurationMs) return false
            if (resolvedAt != other.resolvedAt) return false
            return true
        }

        override fun hashCode(): Int {
            var result = addresses.contentHashCode()
            result = 31 * result + ttlSeconds
            result = 31 * result + viaFallback.hashCode()
            result = 31 * result + lookupDurationMs.hashCode()
            result = 31 * result + resolvedAt.hashCode()
            return result
        }
    }

    private data class WarpSession(
        val mode: String,
        val publicIp: String,
        val establishedAt: Long,
        val lastLatencyMs: Long
    ) {
        companion object {
            fun disabled(): WarpSession = WarpSession("off", "", 0L, 0L)
        }
    }

    class WarpDiagnostics(
        val enabled: Boolean,
        val status: WarpStatus,
        val lastResolver: String?,
        val lastLookupDurationMs: Long,
        val lastUpdatedAt: Long,
        val lastError: String?,
        val cacheHit: Boolean,
        val warpMode: String,
        val lastHealthLatencyMs: Long,
        val sessionEstablishedAt: Long
    ) {
        companion object {
            fun disabled(): WarpDiagnostics = WarpDiagnostics(
                false,
                WarpStatus.DISABLED,
                null,
                0L,
                0L,
                null,
                false,
                "off",
                0L,
                0L
            )
        }
    }

    enum class WarpStatus {
        DISABLED,
        IDLE,
        RESOLVING,
        ACTIVE,
        CACHED,
        BYPASSED,
        ERROR
    }
}
