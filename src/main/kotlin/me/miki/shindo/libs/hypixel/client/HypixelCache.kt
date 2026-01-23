package me.miki.shindo.libs.hypixel.client

import com.google.gson.JsonObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Sistema de cache para dados da API do Hypixel
 * 
 * Funcionalidades:
 * - Cache de dados de jogadores
 * - Expiração automática (5 minutos)
 * - Thread-safe
 * 
 * Extensível para:
 * - Cache persistente em disco
 * - Tamanho de cache configurável
 * - Estratégias de invalidação
 * - Cache de múltiplos tipos de dados
 */
object HypixelCache {

    private data class CachedData(
        val data: JsonObject,
        val timestamp: Long
    )

    private val playerDataCache = ConcurrentHashMap<String, CachedData>()
    private val CACHE_EXPIRY = TimeUnit.MINUTES.toMillis(5) // 5 minutos

    /**
     * Obtém dados de jogador do cache
     */
    fun getPlayerData(uuid: String): JsonObject? {
        val cached = playerDataCache[uuid] ?: return null
        
        // Verifica se expirou
        if (System.currentTimeMillis() - cached.timestamp > CACHE_EXPIRY) {
            playerDataCache.remove(uuid)
            return null
        }
        
        return cached.data
    }

    /**
     * Armazena dados de jogador no cache
     */
    fun cachePlayerData(uuid: String, data: JsonObject) {
        playerDataCache[uuid] = CachedData(data, System.currentTimeMillis())
    }

    /**
     * Limpa o cache
     */
    fun clearCache() {
        playerDataCache.clear()
    }

    /**
     * Remove dados expirados do cache
     */
    fun cleanExpired() {
        val now = System.currentTimeMillis()
        playerDataCache.entries.removeIf { (_, cached) ->
            now - cached.timestamp > CACHE_EXPIRY
        }
    }

    /**
     * Obtém tamanho do cache
     */
    fun getCacheSize(): Int = playerDataCache.size
}
