package me.miki.shindo.libs.hypixel.client

import com.google.gson.JsonObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
object HypixelCache {

    private data class CachedData(
        val data: JsonObject,
        val timestamp: Long
    )

    private val playerDataCache = ConcurrentHashMap<String, CachedData>()
    private val CACHE_EXPIRY = TimeUnit.MINUTES.toMillis(5)
    fun getPlayerData(uuid: String): JsonObject? {
        val cached = playerDataCache[uuid] ?: return null

        if (System.currentTimeMillis() - cached.timestamp > CACHE_EXPIRY) {
            playerDataCache.remove(uuid)
            return null
        }

        return cached.data
    }
    fun cachePlayerData(uuid: String, data: JsonObject) {
        playerDataCache[uuid] = CachedData(data, System.currentTimeMillis())
    }
    fun clearCache() {
        playerDataCache.clear()
    }
    fun cleanExpired() {
        val now = System.currentTimeMillis()
        playerDataCache.entries.removeIf { (_, cached) ->
            now - cached.timestamp > CACHE_EXPIRY
        }
    }
    fun getCacheSize(): Int = playerDataCache.size
}
