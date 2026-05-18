package me.miki.shindo.utils

import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

object PlayerHeadUtils {
    private val cache = ConcurrentHashMap<String, ResourceLocation>()
    private val pending = ConcurrentHashMap<String, Boolean>()

    fun getOrRequest(username: String?): ResourceLocation? {
        val key = normalize(username)
        if (key.isEmpty()) {
            return null
        }
        val rawName = username?.trim() ?: return null

        val cached = cache[key]
        if (cached != null) {
            return cached
        }

        if (pending.putIfAbsent(key, true) == null) {
            TaskExecutor.runAsync(ThreadPoolType.NETWORK) {
                try {
                    val skin = Shindo.getInstance().getSkinManager().downloadSkinByUsername(rawName)
                    val texture = registerTexture(skin.image, "head-$key")
                    if (texture != null) {
                        cache[key] = texture
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

    private fun normalize(username: String?): String = username?.trim()?.lowercase(Locale.ROOT) ?: ""

    private fun registerTexture(
        image: BufferedImage,
        id: String,
    ): ResourceLocation? {
        val mc = Minecraft.getMinecraft()
        return runOnRenderThread {
            val texture = DynamicTexture(image)
            mc.textureManager.getDynamicTextureLocation(id, texture)
        }
    }

    private fun runOnRenderThread(task: () -> ResourceLocation?): ResourceLocation? {
        val mc = Minecraft.getMinecraft()
        if (mc.isCallingFromMinecraftThread) {
            return task.invoke()
        }

        val latch = CountDownLatch(1)
        val ref = AtomicReference<ResourceLocation?>()
        mc.addScheduledTask {
            try {
                ref.set(task.invoke())
            } catch (_: Exception) {
                ref.set(null)
            } finally {
                latch.countDown()
            }
        }
        try {
            latch.await()
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        return ref.get()
    }
}
