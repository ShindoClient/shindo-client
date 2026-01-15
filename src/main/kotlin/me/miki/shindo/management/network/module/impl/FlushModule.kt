package me.miki.shindo.management.network.module.impl

import io.netty.channel.Channel
import me.miki.shindo.management.network.model.NetworkConfig
import me.miki.shindo.management.network.model.NetworkMetrics
import me.miki.shindo.management.network.module.ConfigurableNetworkModule
import me.miki.shindo.management.network.module.NetworkModule
import me.miki.shindo.management.network.module.ToggleableNetworkModule
import net.minecraft.network.Packet
import java.util.Locale

/**
 * Módulo responsável pelo gerenciamento de flush de pacotes.
 * Controla quando os pacotes devem ser enviados para otimizar a latência.
 */
class FlushModule : NetworkModule, ConfigurableNetworkModule<NetworkConfig>, ToggleableNetworkModule {
    override val name: String = "Flush"

    override var enabled: Boolean = true
    private var config: NetworkConfig? = null
    private var metrics: NetworkMetrics? = null

    private var pendingPackets: Int = 0
    private var lastFlushTimestamp: Long = 0L

    companion object {
        private const val MIN_DYNAMIC_INTERVAL_MS = 10
    }

    override fun initialize() {
        lastFlushTimestamp = System.currentTimeMillis()
    }

    override fun update() {
        // Nada a fazer na atualização
    }

    override fun cleanup() {
        pendingPackets = 0
        lastFlushTimestamp = 0L
    }

    override fun applyConfig(config: NetworkConfig) {
        this.config = config
    }

    /**
     * Atualiza as métricas usadas para cálculos dinâmicos.
     */
    fun updateMetrics(metrics: NetworkMetrics) {
        this.metrics = metrics
    }

    /**
     * Processa o envio de um pacote e decide se deve fazer flush.
     */
    fun onSendPacket(channel: Channel?, packet: Packet<*>?) {
        if (channel == null || packet == null) return

        val cfg = config ?: return
        if (!enabled || !cfg.optimizerEnabled) {
            flushChannel(channel)
            return
        }

        if (!cfg.autoFlushEnabled) return

        if (!cfg.burstFlushSmoothing) {
            flushChannel(channel)
            return
        }

        val targetInterval = resolveDynamicInterval(cfg)
        val targetThreshold = resolveDynamicThreshold(cfg)

        pendingPackets++
        val now = System.currentTimeMillis()
        val intervalExceeded = (now - lastFlushTimestamp) >= targetInterval
        val thresholdExceeded = pendingPackets >= targetThreshold

        if (intervalExceeded || thresholdExceeded || isPriorityPacket(packet)) {
            flushChannel(channel)
        }
    }

    private fun flushChannel(channel: Channel) {
        try {
            channel.flush()
        } catch (e: Exception) {
            // Ignorar erros de flush
        } finally {
            pendingPackets = 0
            lastFlushTimestamp = System.currentTimeMillis()
        }
    }

    private fun resolveDynamicInterval(config: NetworkConfig): Int {
        val base = config.flushIntervalMs.coerceAtLeast(MIN_DYNAMIC_INTERVAL_MS)
        if (!config.dynamicFlushEnabled) return base

        val m = metrics ?: return base
        if (m.pingCount == 0) return base

        val average = m.averagePing()
        val jitter = m.jitterPing()
        val jitterImpact = ((jitter * config.jitterSensitivity) / 20).coerceAtMost(12)
        val latencyImpact = if (average > 180) ((average - 180) / 25).coerceAtMost(10) else 0
        val adjusted = (base - jitterImpact - latencyImpact).coerceAtLeast(MIN_DYNAMIC_INTERVAL_MS)

        return adjusted
    }

    private fun resolveDynamicThreshold(config: NetworkConfig): Int {
        val base = config.flushPacketThreshold.coerceAtLeast(1)
        if (!config.dynamicFlushEnabled) return base

        val m = metrics ?: return base
        if (m.pingCount == 0) return base

        val jitter = m.jitterPing()
        return when {
            jitter > 40 -> (base - 2).coerceAtLeast(1)
            jitter > 20 -> (base - 1).coerceAtLeast(1)
            else -> base
        }
    }

    private fun isPriorityPacket(packet: Packet<*>): Boolean {
        val simpleName = packet.javaClass.simpleName.lowercase(Locale.ROOT)
        return simpleName.contains("keepalive") ||
                simpleName.contains("handshake") ||
                simpleName.contains("clientstatus") ||
                simpleName.contains("login")
    }
}
