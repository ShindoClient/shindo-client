package me.miki.shindo.management.network.optimization

/**
 * Configuração para o sistema de otimização de pacotes.
 * Permite personalização dinâmica da classificação de pacotes.
 */
data class PacketConfig(
    /**
     * Se a otimização está habilitada globalmente.
     */
    val optimizationEnabled: Boolean = false,
    
    /**
     * Padrões adicionais para lista negra (pacotes críticos).
     */
    val additionalCriticalPatterns: Set<String> = emptySet(),
    
    /**
     * Classes adicionais para whitelist (processamento paralelo).
     */
    val additionalParallelSafeClasses: Set<String> = emptySet(),
    
    /**
     * Se deve usar cache de classificação.
     */
    val useCache: Boolean = true,
    
    /**
     * Se deve coletar métricas.
     */
    val collectMetrics: Boolean = true,
    
    /**
     * Se deve fazer logging de pacotes processados.
     */
    val enableLogging: Boolean = false
) {
    companion object {
        /**
         * Configuração padrão.
         */
        val DEFAULT = PacketConfig(
            optimizationEnabled = false,
            useCache = true,
            collectMetrics = true,
            enableLogging = false
        )
    }
}
