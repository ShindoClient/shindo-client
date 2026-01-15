package me.miki.shindo.management.network.model

import com.google.gson.JsonObject
import me.miki.shindo.management.network.model.LinkMedium
import me.miki.shindo.utils.JsonUtils

/**
 * Configuração completa do sistema de network.
 * Contém todas as propriedades configuráveis do otimizador de rede.
 */
data class NetworkConfig(
    val optimizerEnabled: Boolean = true,
    val networkMedium: LinkMedium = LinkMedium.WIRED,
    val linkCapacityMbps: Int = 200,
    val aggressiveProfile: Boolean = false,
    val adaptiveBuffering: Boolean = true,
    val tcpNoDelayEnabled: Boolean = true,
    val autoFlushEnabled: Boolean = true,
    val preferNativeTransport: Boolean = true,
    val writeBufferKb: Int = 512,
    val burstFlushSmoothing: Boolean = true,
    val flushIntervalMs: Int = 45,
    val flushPacketThreshold: Int = 4,
    val warpProxyEnabled: Boolean = false,
    val dynamicFlushEnabled: Boolean = true,
    val jitterSensitivity: Int = 6,
    val responsivenessLevel: Int = 6
) {
    companion object {
        private const val MIN_WRITE_BUFFER_KB = 128
        private const val MAX_WRITE_BUFFER_KB = 4096
        private const val MIN_LINK_CAPACITY = 10
        private const val MAX_LINK_CAPACITY = 1000
        private const val MIN_FLUSH_INTERVAL = 10
        private const val MAX_FLUSH_INTERVAL = 120
        private const val MIN_FLUSH_THRESHOLD = 1
        private const val MAX_FLUSH_THRESHOLD = 12
        private const val MIN_JITTER_SENSITIVITY = 1
        private const val MAX_JITTER_SENSITIVITY = 20
        private const val MIN_RESPONSIVENESS = 1
        private const val MAX_RESPONSIVENESS = 10

        fun fromJson(json: JsonObject, defaults: NetworkConfig = NetworkConfig()): NetworkConfig {
            return NetworkConfig(
                optimizerEnabled = JsonUtils.getBooleanProperty(json, "optimizerEnabled", defaults.optimizerEnabled),
                networkMedium = LinkMedium.fromKey(JsonUtils.getStringProperty(json, "networkMedium", defaults.networkMedium.name)),
                linkCapacityMbps = normalizeLinkCapacity(JsonUtils.getIntProperty(json, "linkCapacityMbps", defaults.linkCapacityMbps)),
                aggressiveProfile = JsonUtils.getBooleanProperty(json, "aggressiveProfile", defaults.aggressiveProfile),
                adaptiveBuffering = JsonUtils.getBooleanProperty(json, "adaptiveBuffering", defaults.adaptiveBuffering),
                tcpNoDelayEnabled = JsonUtils.getBooleanProperty(json, "tcpNoDelayEnabled", defaults.tcpNoDelayEnabled),
                autoFlushEnabled = JsonUtils.getBooleanProperty(json, "autoFlushEnabled", defaults.autoFlushEnabled),
                preferNativeTransport = JsonUtils.getBooleanProperty(json, "preferNativeTransport", defaults.preferNativeTransport),
                writeBufferKb = normalizeWriteBuffer(JsonUtils.getIntProperty(json, "writeBufferKb", defaults.writeBufferKb)),
                burstFlushSmoothing = JsonUtils.getBooleanProperty(json, "burstFlushSmoothing", defaults.burstFlushSmoothing),
                flushIntervalMs = normalizeFlushInterval(JsonUtils.getIntProperty(json, "flushIntervalMs", defaults.flushIntervalMs)),
                flushPacketThreshold = normalizeFlushThreshold(JsonUtils.getIntProperty(json, "flushPacketThreshold", defaults.flushPacketThreshold)),
                warpProxyEnabled = JsonUtils.getBooleanProperty(json, "warpProxyEnabled", defaults.warpProxyEnabled),
                dynamicFlushEnabled = JsonUtils.getBooleanProperty(json, "dynamicFlushEnabled", defaults.dynamicFlushEnabled),
                jitterSensitivity = normalizeJitterSensitivity(JsonUtils.getIntProperty(json, "jitterSensitivity", defaults.jitterSensitivity)),
                responsivenessLevel = normalizeResponsiveness(JsonUtils.getIntProperty(json, "responsivenessLevel", defaults.responsivenessLevel))
            )
        }

        fun normalizeWriteBuffer(value: Int): Int = 
            value.coerceIn(MIN_WRITE_BUFFER_KB, MAX_WRITE_BUFFER_KB)

        fun normalizeLinkCapacity(value: Int): Int = 
            value.coerceIn(MIN_LINK_CAPACITY, MAX_LINK_CAPACITY)

        fun normalizeFlushInterval(value: Int): Int = 
            value.coerceIn(MIN_FLUSH_INTERVAL, MAX_FLUSH_INTERVAL)

        fun normalizeFlushThreshold(value: Int): Int = 
            value.coerceIn(MIN_FLUSH_THRESHOLD, MAX_FLUSH_THRESHOLD)

        fun normalizeJitterSensitivity(value: Int): Int = 
            value.coerceIn(MIN_JITTER_SENSITIVITY, MAX_JITTER_SENSITIVITY)

        fun normalizeResponsiveness(value: Int): Int = 
            value.coerceIn(MIN_RESPONSIVENESS, MAX_RESPONSIVENESS)
    }

    fun toJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("optimizerEnabled", optimizerEnabled)
        json.addProperty("networkMedium", networkMedium.name)
        json.addProperty("linkCapacityMbps", linkCapacityMbps)
        json.addProperty("aggressiveProfile", aggressiveProfile)
        json.addProperty("adaptiveBuffering", adaptiveBuffering)
        json.addProperty("tcpNoDelayEnabled", tcpNoDelayEnabled)
        json.addProperty("autoFlushEnabled", autoFlushEnabled)
        json.addProperty("preferNativeTransport", preferNativeTransport)
        json.addProperty("writeBufferKb", writeBufferKb)
        json.addProperty("burstFlushSmoothing", burstFlushSmoothing)
        json.addProperty("flushIntervalMs", flushIntervalMs)
        json.addProperty("flushPacketThreshold", flushPacketThreshold)
        json.addProperty("warpProxyEnabled", warpProxyEnabled)
        json.addProperty("dynamicFlushEnabled", dynamicFlushEnabled)
        json.addProperty("jitterSensitivity", jitterSensitivity)
        json.addProperty("responsivenessLevel", responsivenessLevel)
        return json
    }

    fun copyWithOptimizerDisabled(): NetworkConfig {
        return copy(
            tcpNoDelayEnabled = false,
            autoFlushEnabled = false,
            preferNativeTransport = true,
            writeBufferKb = 256,
            burstFlushSmoothing = false,
            flushIntervalMs = 50,
            flushPacketThreshold = 6,
            dynamicFlushEnabled = false,
            jitterSensitivity = 6
        )
    }
}
