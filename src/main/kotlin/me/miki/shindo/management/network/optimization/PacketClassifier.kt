package me.miki.shindo.management.network.optimization

import net.minecraft.network.Packet
import java.util.concurrent.ConcurrentHashMap

/**
 * Classifica pacotes de rede para determinar o tipo de processamento adequado.
 * Suporta configuração dinâmica e cache para melhor performance.
 */
object PacketClassifier {
    
    /**
     * Lista negra padrão de padrões de nomes de classes que indicam pacotes críticos.
     * Estes pacotes NUNCA devem ser processados em paralelo.
     */
    private val DEFAULT_CRITICAL_PATTERNS = setOf(
        "Chunk",
        "JoinGame",
        "Respawn",
        "Spawn",
        "Destroy",
        "Explosion",
        "BlockChange",
        "MultiBlockChange",
        "MapChunk",
        "KeepAlive",
        "PlayerPosLook",
        "EntityVelocity",
        "EntityHeadLook",
        "EntityEquipment"
    )
    
    /**
     * Whitelist padrão de nomes de classes que são seguros para processamento paralelo.
     */
    private val DEFAULT_PARALLEL_SAFE_CLASSES = setOf(
        // Chat e comunicação
        "S02PacketChat",
        
        // Efeitos visuais e sonoros
        "S29PacketSoundEffect",
        "S2APacketParticles",
        "S28PacketEffect",
        
        // Tempo e mundo (não críticos)
        "S03PacketTimeUpdate",
        "S05PacketSpawnPosition",
        "S41PacketServerDifficulty",
        
        // Inventário
        "S30PacketWindowItems",
        "S2FPacketSetSlot"
    )
    
    // Configuração dinâmica
    private var config: PacketConfig = PacketConfig.DEFAULT
    private val criticalPatterns = ConcurrentHashMap<String, Boolean>()
    private val parallelSafeClasses = ConcurrentHashMap<String, Boolean>()
    
    init {
        // Inicializa com padrões padrão
        updateConfig(PacketConfig.DEFAULT)
    }
    
    /**
     * Atualiza a configuração do classificador.
     */
    fun updateConfig(newConfig: PacketConfig) {
        config = newConfig
        
        // Atualiza lista negra
        criticalPatterns.clear()
        (DEFAULT_CRITICAL_PATTERNS + newConfig.additionalCriticalPatterns).forEach {
            criticalPatterns[it] = true
        }
        
        // Atualiza whitelist
        parallelSafeClasses.clear()
        (DEFAULT_PARALLEL_SAFE_CLASSES + newConfig.additionalParallelSafeClasses).forEach {
            parallelSafeClasses[it] = true
        }
        
        // Limpa cache se necessário
        if (!newConfig.useCache) {
            PacketCache.clear()
        }
    }
    
    /**
     * Classifica um pacote e retorna seu tipo de processamento.
     */
    fun classify(packet: Packet<*>): PacketType {
        // Usa cache se habilitado
        if (config.useCache) {
            return PacketCache.getClassification(packet)
        }
        
        return classifyInternal(packet)
    }
    
    /**
     * Classificação interna sem cache.
     */
    private fun classifyInternal(packet: Packet<*>): PacketType {
        val packetClass = packet.javaClass.simpleName
        
        // Verifica lista negra primeiro (mais importante)
        if (criticalPatterns.keys.any { packetClass.contains(it) }) {
            return PacketType.CRITICAL
        }
        
        // Verifica whitelist
        if (parallelSafeClasses.containsKey(packetClass)) {
            return PacketType.PARALLEL_SAFE
        }
        
        // Por padrão, processa sequencialmente (mais seguro)
        return PacketType.SEQUENTIAL
    }
    
    /**
     * Verifica se um pacote pode ser processado em paralelo.
     */
    fun canProcessInParallel(packet: Packet<*>): Boolean {
        if (config.useCache) {
            return PacketCache.canProcessInParallel(packet)
        }
        return classifyInternal(packet) == PacketType.PARALLEL_SAFE
    }
    
    /**
     * Verifica se um pacote é crítico e deve ser processado sequencialmente.
     */
    fun isCritical(packet: Packet<*>): Boolean {
        if (config.useCache) {
            return PacketCache.isCritical(packet)
        }
        return classifyInternal(packet) == PacketType.CRITICAL
    }
    
    /**
     * Adiciona um padrão à lista negra dinamicamente.
     */
    fun addCriticalPattern(pattern: String) {
        criticalPatterns[pattern] = true
        PacketCache.clear() // Limpa cache quando configuração muda
    }
    
    /**
     * Remove um padrão da lista negra.
     */
    fun removeCriticalPattern(pattern: String) {
        criticalPatterns.remove(pattern)
        PacketCache.clear()
    }
    
    /**
     * Adiciona uma classe à whitelist dinamicamente.
     */
    fun addParallelSafeClass(className: String) {
        parallelSafeClasses[className] = true
        PacketCache.clear()
    }
    
    /**
     * Remove uma classe da whitelist.
     */
    fun removeParallelSafeClass(className: String) {
        parallelSafeClasses.remove(className)
        PacketCache.clear()
    }
    
    /**
     * Obtém a configuração atual.
     */
    fun getConfig(): PacketConfig = config
}
