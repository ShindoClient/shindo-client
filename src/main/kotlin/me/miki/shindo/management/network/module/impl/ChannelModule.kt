package me.miki.shindo.management.network.module.impl

import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import me.miki.shindo.management.network.model.NetworkConfig
import me.miki.shindo.management.network.module.ConfigurableNetworkModule
import me.miki.shindo.management.network.module.NetworkModule
import me.miki.shindo.management.network.module.ToggleableNetworkModule

/**
 * Módulo responsável pelo gerenciamento de canais de rede.
 * Aplica configurações TCP e buffer ao canal ativo.
 */
class ChannelModule : NetworkModule, ConfigurableNetworkModule<NetworkConfig>, ToggleableNetworkModule {
    override val name: String = "Channel"

    override var enabled: Boolean = true
    private var config: NetworkConfig? = null
    private var activeChannel: Channel? = null

    override fun initialize() {
        // Nada a fazer na inicialização
    }

    override fun update() {
        // Nada a fazer na atualização
    }

    override fun cleanup() {
        activeChannel = null
    }

    override fun applyConfig(config: NetworkConfig) {
        this.config = config
        activeChannel?.let { applyToChannel(it) }
    }

    /**
     * Aplica o canal ativo e configura suas opções.
     */
    fun applyChannel(channel: Channel?) {
        if (channel == null) {
            activeChannel = null
            return
        }

        activeChannel = channel
        applyToChannel(channel)
    }

    private fun applyToChannel(channel: Channel) {
        val cfg = config ?: return
        if (!enabled || !cfg.optimizerEnabled) return

        try {
            // Aplica TCP_NODELAY
            channel.config().setOption(ChannelOption.TCP_NODELAY, cfg.tcpNoDelayEnabled)
        } catch (e: Exception) {
            // Ignorar erros de configuração
        }

        // Aplica configuração de buffer
        applyBufferConfiguration(channel, cfg.writeBufferKb)
    }

    private fun applyBufferConfiguration(channel: Channel, bufferKb: Int) {
        val minBufferKb = 128
        val highWaterMark = bufferKb.coerceAtLeast(minBufferKb) * 1024
        val lowWaterMark = (32 * 1024).coerceAtLeast(highWaterMark / 2)

        try {
            channel.config().setOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, highWaterMark)
            channel.config().setOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, lowWaterMark)
        } catch (e: Exception) {
            // Ignorar erros de configuração
        }
    }

    fun getActiveChannel(): Channel? = activeChannel
}
