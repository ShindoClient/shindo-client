package me.miki.shindo.management.network.model

/**
 * Perfil de otimização de rede calculado a partir da configuração.
 * Contém valores calculados e recomendações baseadas no perfil.
 */
data class NetworkProfile(
    val latencyFocus: Float,
    val stabilityFocus: Float,
    val throughputFocus: Float,
    val recommendedBufferKb: Int,
    val baseFlushInterval: Int,
    val baseFlushThreshold: Int,
    val responsivenessBoost: Int
) {
    companion object {
        fun calculate(
            config: NetworkConfig,
            currentBufferKb: Int,
            maxBufferKb: Int = 4096
        ): NetworkProfile {
            val latencyFocus = calculateLatencyFocus(config)
            val stabilityFocus = calculateStabilityFocus(config, latencyFocus)
            val throughputFocus = calculateThroughputFocus(
                config,
                currentBufferKb,
                maxBufferKb,
                latencyFocus,
                stabilityFocus
            )
            val recommendedBuffer = computeRecommendedBuffer(config)
            val baseInterval = calculateBaseFlushInterval(config)
            val baseThreshold = calculateBaseFlushThreshold(config)
            val responsivenessBoost = calculateResponsivenessBoost(config)

            return NetworkProfile(
                latencyFocus = latencyFocus,
                stabilityFocus = stabilityFocus,
                throughputFocus = throughputFocus,
                recommendedBufferKb = recommendedBuffer,
                baseFlushInterval = baseInterval,
                baseFlushThreshold = baseThreshold,
                responsivenessBoost = responsivenessBoost
            )
        }

        private fun calculateLatencyFocus(config: NetworkConfig): Float {
            val mediumBias = when (config.networkMedium) {
                LinkMedium.WIRED -> 0.75f
                LinkMedium.WIRELESS -> 0.6f
                LinkMedium.MOBILE -> 0.5f
            }
            val aggressiveBias = if (config.aggressiveProfile) 0.15f else 0.0f
            return (mediumBias + aggressiveBias).coerceIn(0f, 1f)
        }

        private fun calculateStabilityFocus(
            config: NetworkConfig,
            latencyFocus: Float
        ): Float {
            val mediumBase = when (config.networkMedium) {
                LinkMedium.WIRED -> 0.8f
                LinkMedium.WIRELESS -> 0.65f
                LinkMedium.MOBILE -> 0.55f
            }
            val adaptiveBonus = if (config.adaptiveBuffering) 0.1f else 0.0f
            val smoothingBonus = if (config.burstFlushSmoothing) 0.05f else 0.0f
            return ((mediumBase + adaptiveBonus + smoothingBonus) - (latencyFocus * 0.25f))
                .coerceIn(0f, 1f)
        }

        private fun calculateThroughputFocus(
            config: NetworkConfig,
            currentBufferKb: Int,
            maxBufferKb: Int,
            latencyFocus: Float,
            stabilityFocus: Float
        ): Float {
            val bufferRatio = (currentBufferKb.toFloat() / maxBufferKb)
            val capacityRatio = (config.linkCapacityMbps / 750f).coerceAtMost(1f)
            val base = (bufferRatio * 0.4f) + (capacityRatio * 0.6f)
            return (base - (latencyFocus * 0.2f) + (stabilityFocus * 0.15f))
                .coerceIn(0f, 1f)
        }

        private fun computeRecommendedBuffer(config: NetworkConfig): Int {
            val base = when (config.networkMedium) {
                LinkMedium.WIRED -> 512
                LinkMedium.WIRELESS -> 384
                LinkMedium.MOBILE -> 256
            }

            val capacity = config.linkCapacityMbps.coerceIn(10, 1000)
            val capacityFactor = kotlin.math.sqrt(capacity.toDouble()) * 
                (if (config.aggressiveProfile) 24.0 else 16.0)
            
            var recommended = (base + capacityFactor).toInt()
            if (config.aggressiveProfile) {
                recommended += 128
            }
            recommended += calculateResponsivenessBoost(config) * 12
            
            return NetworkConfig.normalizeWriteBuffer(recommended)
        }

        private fun calculateBaseFlushInterval(config: NetworkConfig): Int {
            return when (config.networkMedium) {
                LinkMedium.WIRED -> if (config.aggressiveProfile) 26 else 36
                LinkMedium.WIRELESS -> if (config.aggressiveProfile) 32 else 44
                LinkMedium.MOBILE -> if (config.aggressiveProfile) 38 else 52
            }
        }

        private fun calculateBaseFlushThreshold(config: NetworkConfig): Int {
            return when (config.networkMedium) {
                LinkMedium.WIRED -> if (config.aggressiveProfile) 3 else 4
                LinkMedium.WIRELESS -> if (config.aggressiveProfile) 4 else 5
                LinkMedium.MOBILE -> if (config.aggressiveProfile) 5 else 6
            }
        }

        private fun calculateResponsivenessBoost(config: NetworkConfig): Int {
            return (config.responsivenessLevel - 6).coerceAtLeast(0)
        }
    }
}
