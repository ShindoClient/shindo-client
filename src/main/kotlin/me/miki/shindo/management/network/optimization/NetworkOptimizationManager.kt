package me.miki.shindo.management.network.optimization

import me.miki.shindo.management.mods.impl.InternalSettingsMod
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet

/**
 * Gerenciador central para otimizações de rede e processamento de pacotes.
 * Coordena a classificação e processamento otimizado de pacotes.
 * Gerencia métricas, cache e configuração dinâmica.
 */
object NetworkOptimizationManager {
    
    private val metricsBuilder = PacketMetricsBuilder()
    private var config: PacketConfig = PacketConfig.DEFAULT
    private var packetProcessor: PacketProcessor = PacketProcessor(
        metricsBuilder = if (config.collectMetrics) metricsBuilder else null,
        enableLogging = config.enableLogging
    )
    
    init {
        // Inicializa com configuração padrão
        updateConfig(PacketConfig.DEFAULT)
    }
    
    /**
     * Atualiza a configuração do sistema.
     */
    fun updateConfig(newConfig: PacketConfig) {
        config = newConfig
        
        // Atualiza classificador
        PacketClassifier.updateConfig(newConfig)
        
        // Recria processor com nova configuração
        packetProcessor = PacketProcessor(
            metricsBuilder = if (newConfig.collectMetrics) metricsBuilder else null,
            enableLogging = newConfig.enableLogging
        )
    }
    
    /**
     * Obtém a configuração atual.
     */
    fun getConfig(): PacketConfig = config
    
    /**
     * Verifica se a otimização de rede está habilitada.
     */
    fun isOptimizationEnabled(): Boolean {
        val settingEnabled = InternalSettingsMod.instance?.networkOptimizationSetting == true
        return settingEnabled && config.optimizationEnabled
    }
    
    /**
     * Processa um pacote de forma otimizada.
     * 
     * @param packet O pacote a ser processado
     * @param handler O handler de rede
     */
    fun processPacket(packet: Packet<INetHandler>, handler: INetHandler) {
        // Verifica se é um handler de cliente
        if (handler !is NetHandlerPlayClient) {
            // Para handlers não-client, sempre processa sequencialmente
            packet.processPacket(handler)
            return
        }
        
        // Verifica se o canal está aberto
        if (!handler.networkManager.isChannelOpen()) {
            return
        }
        
        // Processa o pacote usando o processor otimizado
        val optimizationEnabled = isOptimizationEnabled()
        packetProcessor.processAndWait(packet, handler, optimizationEnabled)
    }
    
    /**
     * Classifica um pacote sem processá-lo.
     * Útil para logging, métricas, ou debug.
     */
    fun classifyPacket(packet: Packet<*>): PacketType {
        return PacketClassifier.classify(packet)
    }
    
    /**
     * Verifica se um pacote pode ser processado em paralelo.
     */
    fun canProcessInParallel(packet: Packet<*>): Boolean {
        return PacketClassifier.canProcessInParallel(packet)
    }
    
    /**
     * Verifica se um pacote é crítico.
     */
    fun isCriticalPacket(packet: Packet<*>): Boolean {
        return PacketClassifier.isCritical(packet)
    }
    
    /**
     * Obtém métricas atuais de processamento.
     */
    fun getMetrics(): PacketMetrics {
        return metricsBuilder.build()
    }
    
    /**
     * Reseta as métricas.
     */
    fun resetMetrics() {
        metricsBuilder.reset()
    }
    
    /**
     * Obtém estatísticas do cache.
     */
    fun getCacheStats(): PacketCache.CacheStats {
        return PacketCache.getCacheStats()
    }
    
    /**
     * Limpa o cache de classificação.
     */
    fun clearCache() {
        PacketCache.clear()
    }
    
    /**
     * Adiciona um padrão à lista negra dinamicamente.
     */
    fun addCriticalPattern(pattern: String) {
        PacketClassifier.addCriticalPattern(pattern)
    }
    
    /**
     * Remove um padrão da lista negra.
     */
    fun removeCriticalPattern(pattern: String) {
        PacketClassifier.removeCriticalPattern(pattern)
    }
    
    /**
     * Adiciona uma classe à whitelist dinamicamente.
     */
    fun addParallelSafeClass(className: String) {
        PacketClassifier.addParallelSafeClass(className)
    }
    
    /**
     * Remove uma classe da whitelist.
     */
    fun removeParallelSafeClass(className: String) {
        PacketClassifier.removeParallelSafeClass(className)
    }
}
