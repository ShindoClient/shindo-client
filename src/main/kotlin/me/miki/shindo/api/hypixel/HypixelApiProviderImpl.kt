package me.miki.shindo.api.hypixel

import com.google.gson.JsonObject
import me.miki.shindo.addon.api.hypixel.HypixelApiProvider
import me.miki.shindo.addon.api.hypixel.PitEvent
import me.miki.shindo.addon.api.hypixel.PitItem
import me.miki.shindo.addon.api.hypixel.PitPlayerStats
import me.miki.shindo.Shindo
import me.miki.shindo.libs.hypixel.client.HypixelHttpClient
import me.miki.shindo.libs.hypixel.exceptions.HypixelApiException
import me.miki.shindo.logger.ShindoLogger
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Implementação concreta de [HypixelApiProvider] usada pelo Shindo Client.
 *
 * Toda integração HTTP, cache e rate limit é feita aqui, nunca nos addons.
 */
class HypixelApiProviderImpl : HypixelApiProvider {

    private data class CachedEntry<T>(
        val data: T,
        val timestamp: Long
    )

    private val pitStatsCache = ConcurrentHashMap<UUID, CachedEntry<PitPlayerStats>>()
    private val pitEventsCache = ConcurrentHashMap<String, CachedEntry<List<PitEvent>>>()
    private val enderChestCache = ConcurrentHashMap<UUID, CachedEntry<List<PitItem>>>()

    private val pitStatsTtl = TimeUnit.SECONDS.toMillis(45)
    private val pitEventsTtl = TimeUnit.SECONDS.toMillis(30)
    private val enderChestTtl = TimeUnit.MINUTES.toMillis(3)

    override fun getPlayerPitStats(uuid: UUID): PitPlayerStats? {
        val now = System.currentTimeMillis()
        pitStatsCache[uuid]?.let { cached ->
            if (now - cached.timestamp <= pitStatsTtl) {
                return cached.data
            }
        }

        return try {
            val json = HypixelHttpClient.getPlayerDataCached(uuid.toString().replace("-", "")) ?: return null
            val stats = parsePitStats(json) ?: return null
            pitStatsCache[uuid] = CachedEntry(stats, now)
            stats
        } catch (e: HypixelApiException) {
            ShindoLogger.error("[HypixelAPI] Failed to fetch Pit stats for $uuid: ${e.message}", e)
            null
        } catch (e: Exception) {
            ShindoLogger.error("[HypixelAPI] Unexpected error while fetching Pit stats for $uuid", e)
            null
        }
    }

    override fun getUpcomingPitEvents(): List<PitEvent> {
        val cacheKey = "pit_events"
        val now = System.currentTimeMillis()
        pitEventsCache[cacheKey]?.let { cached ->
            if (now - cached.timestamp <= pitEventsTtl) {
                return cached.data
            }
        }

        return try {
            // TODO: integrar com fonte oficial de eventos (API/Nadeshiko/BrookeAFK).
            val events = emptyList<PitEvent>()
            pitEventsCache[cacheKey] = CachedEntry(events, now)
            events
        } catch (e: Exception) {
            ShindoLogger.error("[HypixelAPI] Failed to fetch Pit events", e)
            emptyList()
        }
    }

    override fun getEnderChest(uuid: UUID): List<PitItem> {
        val now = System.currentTimeMillis()
        enderChestCache[uuid]?.let { cached ->
            if (now - cached.timestamp <= enderChestTtl) {
                return cached.data
            }
        }

        return try {
            // Placeholder: integração real com endpoint de EnderChest será adicionada aqui.
            val items = emptyList<PitItem>()
            enderChestCache[uuid] = CachedEntry(items, now)
            items
        } catch (e: Exception) {
            ShindoLogger.error("[HypixelAPI] Failed to fetch EnderChest for $uuid", e)
            emptyList()
        }
    }

    private fun parsePitStats(playerJson: JsonObject): PitPlayerStats? {
        // Estrutura exata do JSON pode variar; este método deve ser mantido
        // apenas dentro do client. Para agora, fazemos um parsing defensivo.
        val player = playerJson.getAsJsonObject("player") ?: return null
        val stats = player.getAsJsonObject("stats") ?: return null
        val pit = stats.getAsJsonObject("Pit") ?: stats.getAsJsonObject("Pit") ?: return null

        val profile = pit.getAsJsonObject("profile") ?: pit

        val level = profile.get("level")?.asInt ?: 0
        val prestige = profile.get("prestige")?.asInt ?: 0
        val currentXp = profile.get("xp")?.asLong ?: 0L
        val xpForNext = profile.get("xp_for_next")?.asLong ?: 0L
        val gold = profile.get("gold")?.asLong ?: 0L
        val renown = profile.get("renown")?.asLong ?: 0L

        return PitPlayerStats(
            level = level,
            prestige = prestige,
            currentXp = currentXp,
            xpForNextLevel = xpForNext,
            gold = gold,
            renown = renown
        )
    }

    fun cleanExpired() {
        val now = System.currentTimeMillis()
        pitStatsCache.entries.removeIf { now - it.value.timestamp > pitStatsTtl }
        pitEventsCache.entries.removeIf { now - it.value.timestamp > pitEventsTtl }
        enderChestCache.entries.removeIf { now - it.value.timestamp > enderChestTtl }
    }
}

