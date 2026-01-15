package me.miki.shindo.management.network.model

import me.miki.shindo.management.network.proxy.WarpProxyManager

/**
 * Snapshot completo do estado atual do sistema de network.
 * Usado para exibição na UI e diagnóstico.
 */
data class NetworkSnapshot(
    val config: NetworkConfig,
    val profile: NetworkProfile,
    val metrics: NetworkMetrics,
    val dynamicIntervalMs: Int,
    val dynamicThreshold: Int,
    val warpInfo: WarpInfo
) {
    companion object {
        fun create(
            config: NetworkConfig,
            metrics: NetworkMetrics,
            currentBufferKb: Int,
            dynamicInterval: Int,
            dynamicThreshold: Int,
            warpDiagnostics: WarpProxyManager.WarpDiagnostics?
        ): NetworkSnapshot {
            val profile = NetworkProfile.calculate(config, currentBufferKb)
            val warpInfo = WarpInfo.fromDiagnostics(warpDiagnostics, config.warpProxyEnabled)
            
            return NetworkSnapshot(
                config = config,
                profile = profile,
                metrics = metrics,
                dynamicIntervalMs = dynamicInterval,
                dynamicThreshold = dynamicThreshold,
                warpInfo = warpInfo
            )
        }
    }
}

/**
 * Informações sobre o proxy WARP.
 */
data class WarpInfo(
    val enabled: Boolean,
    val status: WarpProxyManager.WarpStatus,
    val resolver: String?,
    val lookupMs: Long,
    val lastUpdatedAt: Long,
    val cacheHit: Boolean,
    val error: String?
) {
    companion object {
        fun fromDiagnostics(
            diagnostics: WarpProxyManager.WarpDiagnostics?,
            configEnabled: Boolean
        ): WarpInfo {
            if (diagnostics == null) {
                return WarpInfo(
                    enabled = configEnabled,
                    status = if (configEnabled) WarpProxyManager.WarpStatus.IDLE 
                            else WarpProxyManager.WarpStatus.DISABLED,
                    resolver = null,
                    lookupMs = 0L,
                    lastUpdatedAt = 0L,
                    cacheHit = false,
                    error = null
                )
            }
            
            return WarpInfo(
                enabled = diagnostics.isEnabled() && configEnabled,
                status = diagnostics.getStatus(),
                resolver = diagnostics.getLastResolver(),
                lookupMs = diagnostics.getLastLookupDurationMs(),
                lastUpdatedAt = diagnostics.getLastUpdatedAt(),
                cacheHit = diagnostics.isCacheHit(),
                error = diagnostics.getLastError()
            )
        }
    }
}

