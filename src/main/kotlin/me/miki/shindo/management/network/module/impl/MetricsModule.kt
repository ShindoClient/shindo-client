package me.miki.shindo.management.network.module.impl

import me.miki.shindo.management.network.model.NetworkMetrics
import me.miki.shindo.management.network.module.NetworkModule
import me.miki.shindo.management.network.module.ToggleableNetworkModule
import net.minecraft.client.Minecraft

/**
 * Módulo responsável pela coleta de métricas de rede.
 * Coleta ping e calcula estatísticas como jitter e média.
 */
class MetricsModule : NetworkModule, ToggleableNetworkModule {
    override val name: String = "Metrics"

    override var enabled: Boolean = true

    private var metrics = NetworkMetrics()
    private val mc = Minecraft.getMinecraft()

    override fun initialize() {
        metrics = NetworkMetrics()
    }

    override fun update() {
        if (!enabled) return
        pollPing()
    }

    override fun cleanup() {
        metrics = NetworkMetrics()
    }

    /**
     * Obtém as métricas atuais.
     */
    fun getMetrics(): NetworkMetrics = metrics

    /**
     * Reseta as métricas.
     */
    fun reset() {
        metrics = NetworkMetrics()
    }

    private fun pollPing() {
        if (mc.thePlayer == null || mc.netHandler == null) return

        val now = System.currentTimeMillis()
        if (!metrics.shouldPoll(now)) return

        try {
            val playerInfo = mc.netHandler.getPlayerInfo(mc.thePlayer.uniqueID) ?: return
            val ping = playerInfo.responseTime.coerceAtLeast(1)
            metrics = metrics.addPingSample(ping).withPollTime(now)
        } catch (e: Exception) {
            // Ignorar erros de polling
        }
    }
}
