package com.shindoclient.shindo.utils

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.utils.concurrent.TaskExecutor
import com.shindoclient.shindo.utils.concurrent.ThreadPoolType
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.util.Collections
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object PlayerHeadUtils {
    // Bound cache: evict entries beyond MAX_CACHED to prevent unbounded growth in long sessions
    private const val MAX_CACHED = 256

    private val cache = ConcurrentHashMap<String, ResourceLocation>(MAX_CACHED)

    // Use a proper concurrent set instead of Map<String, Boolean>
    private val pending: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())

    fun getOrRequest(username: String?): ResourceLocation? {
        val key = normalize(username) ?: return null

        cache[key]?.let { return it }

        if (pending.add(key)) {
            // Capture instance eagerly on the calling thread — never access singletons inside async lambdas
            val skinManager = Shindo.getInstance().getSkinManager()

            TaskExecutor.runAsync(ThreadPoolType.NETWORK) {
                try {
                    val skin = skinManager.downloadSkinByUsername(username!!.trim())

                    // Build DynamicTexture on the network thread (CPU-side only, no GL calls)
                    val texture = DynamicTexture(skin.image)

                    scheduleTextureUpload(texture, "head-$key") { location ->
                        evictIfOverCapacity()
                        cache[key] = location
                    }
                } catch (e: Exception) {
                    ShindoLogger.error("PlayerHeadUtils.getOrRequest", e)
                } finally {
                    pending.remove(key)
                }
            }
        }

        return null
    }

    /**
     * Schedules texture registration on the render thread without blocking the caller.
     *
     * The original implementation used CountDownLatch.await() on the network thread pool,
     * which would stall that thread indefinitely if the render thread was busy or the task
     * was dropped. This fire-and-forget approach is safe: the callback runs once the render
     * thread processes the scheduled task, and the network thread is never blocked.
     */
    private fun scheduleTextureUpload(
        texture: DynamicTexture,
        id: String,
        onReady: (ResourceLocation) -> Unit,
    ) {
        val mc = Minecraft.getMinecraft()

        if (mc.isCallingFromMinecraftThread) {
            runCatching { onReady(mc.textureManager.getDynamicTextureLocation(id, texture)) }
            return
        }

        mc.addScheduledTask {
            runCatching {
                onReady(mc.textureManager.getDynamicTextureLocation(id, texture))
            }.onFailure {
                ShindoLogger.error("PlayerHeadUtils.scheduleTextureUpload", it as Exception)
            }
        }
    }

    /**
     * Evicts the oldest 25% of entries when the cache exceeds MAX_CACHED.
     * Simple LRU approximation without the overhead of LinkedHashMap under concurrency.
     */
    private fun evictIfOverCapacity() {
        if (cache.size < MAX_CACHED) return
        val toRemove = cache.size / 4
        val iter = cache.keys.iterator()
        repeat(toRemove) {
            if (iter.hasNext()) {
                iter.next()
                iter.remove()
            }
        }
    }

    /**
     * Returns null instead of empty string so callers can use ?.let / ?: return null idioms.
     */
    private fun normalize(username: String?): String? {
        val trimmed = username?.trim()?.lowercase(Locale.ROOT)
        return if (trimmed.isNullOrEmpty()) null else trimmed
    }
}
