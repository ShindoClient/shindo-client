package me.miki.shindo.management.addons.resourcify.cache

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.Multithreading
import me.miki.shindo.utils.network.HttpUtils
import me.miki.shindo.utils.network.UserAgents
import java.awt.image.BufferedImage
import java.io.File
import java.security.MessageDigest
import java.util.Collections
import javax.imageio.ImageIO

class ResourcifyIconCache(private val cacheDir: File) {

    private val inFlight = Collections.synchronizedSet(HashSet<String>())

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    fun getIconFile(url: String?): File? {
        if (url.isNullOrBlank()) return null
        val target = resolveFile(url)
        if (target.exists()) {
            return target
        }
        queueDownload(url, target)
        return null
    }

    private fun queueDownload(url: String, target: File) {
        if (!inFlight.add(url)) return
        Multithreading.runAsync(Runnable {
            try {
                val connection = HttpUtils.setupConnection(url, UserAgents.MOZILLA, 8000, false) ?: return@Runnable
                connection.inputStream.use { stream ->
                    val image = ImageIO.read(stream)
                    if (image != null) {
                        writePng(image, target)
                    }
                }
            } catch (e: Exception) {
                ShindoLogger.error("Failed to cache icon for $url", e)
            } finally {
                inFlight.remove(url)
            }
        })
    }

    private fun writePng(image: BufferedImage, target: File) {
        try {
            target.parentFile?.mkdirs()
            ImageIO.write(image, "png", target)
        } catch (e: Exception) {
            ShindoLogger.error("Failed to write icon cache ${target.name}", e)
        }
    }

    private fun resolveFile(url: String): File {
        val hash = sha1(url)
        return File(cacheDir, "$hash.png")
    }

    private fun sha1(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(input.toByteArray(Charsets.UTF_8))
        val sb = StringBuilder()
        for (b in digest) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}
