package me.miki.shindo.management.network.optimization

import net.minecraft.network.Packet
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache para classificação de pacotes.
 * Melhora performance evitando reclassificação de pacotes do mesmo tipo.
 */
object PacketCache {
    
    private val classificationCache = ConcurrentHashMap<Class<*>, PacketType>(64)
    private val canParallelCache = ConcurrentHashMap<Class<*>, Boolean>(64)
    private val isCriticalCache = ConcurrentHashMap<Class<*>, Boolean>(64)
    
    /**
     * Obtém ou calcula a classificação de um pacote.
     */
    fun getClassification(packet: Packet<*>): PacketType {
        return classificationCache.computeIfAbsent(packet.javaClass) {
            PacketClassifier.classify(packet)
        }
    }
    
    /**
     * Obtém ou calcula se um pacote pode ser processado em paralelo.
     */
    fun canProcessInParallel(packet: Packet<*>): Boolean {
        return canParallelCache.computeIfAbsent(packet.javaClass) {
            PacketClassifier.canProcessInParallel(packet)
        }
    }
    
    /**
     * Obtém ou calcula se um pacote é crítico.
     */
    fun isCritical(packet: Packet<*>): Boolean {
        return isCriticalCache.computeIfAbsent(packet.javaClass) {
            PacketClassifier.isCritical(packet)
        }
    }
    
    /**
     * Limpa o cache.
     * Útil quando a configuração de classificação muda.
     */
    fun clear() {
        classificationCache.clear()
        canParallelCache.clear()
        isCriticalCache.clear()
    }
    
    /**
     * Obtém estatísticas do cache.
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            classificationSize = classificationCache.size,
            canParallelSize = canParallelCache.size,
            isCriticalSize = isCriticalCache.size
        )
    }
    
    data class CacheStats(
        val classificationSize: Int,
        val canParallelSize: Int,
        val isCriticalSize: Int
    )
}
