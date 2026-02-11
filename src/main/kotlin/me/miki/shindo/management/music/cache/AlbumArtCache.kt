package me.miki.shindo.management.music.cache

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.file.FileManager
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.time.Duration
import java.util.Arrays
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer
import java.util.function.Supplier
import javax.imageio.ImageIO
class AlbumArtCache(private val fileManager: FileManager) : AutoCloseable {

    private val cacheDir: File = File(fileManager.musicDir, CACHE_DIR)
    private val inProgressDownloads = ConcurrentHashMap<String, CompletableFuture<String>>()
    private val downloadExecutor = ThreadPoolExecutor(
        1, MAX_CONCURRENT_DOWNLOADS,
        60L, TimeUnit.SECONDS,
        LinkedBlockingQueue(),
        object : ThreadFactory {
            private val defaultFactory = Executors.defaultThreadFactory()
            override fun newThread(r: Runnable): Thread {
                val thread = defaultFactory.newThread(r)
                thread.isDaemon = true
                thread.name = "AlbumArt-Download-${thread.id}"
                return thread
            }
        }
    )
    private val maintenanceExecutor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r).apply {
                name = "AlbumArt-Maintenance"
                isDaemon = true
            }
        }

    init {
        initializeCache()
        scheduleMaintenance()
    }

    private fun initializeCache() {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            ShindoLogger.error("Failed to create album art cache directory")
        }
    }

    private fun scheduleMaintenance() {
        maintenanceExecutor.scheduleAtFixedRate(
            this::performMaintenance,
            1, 24, TimeUnit.HOURS
        )
    }

    fun getCachedAlbumArtUrlAsync(id: String?, imageUrl: String?): CompletableFuture<String> {
        return inProgressDownloads.computeIfAbsent(id!!) { key: String? ->
            val cachedFile = getCacheFile(id)
            if (cachedFile.exists() && isValidCacheFile(cachedFile)) {
                return@computeIfAbsent CompletableFuture.completedFuture(cachedFile.absolutePath)
            }
            CompletableFuture.supplyAsync(
                Supplier { downloadAndCacheImage(id, imageUrl!!) },
                downloadExecutor
            ).whenComplete { result: String?, ex: Throwable? ->
                inProgressDownloads.remove(
                    id
                )
            }
        }
    }

    fun getAlbumArt(imageUrl: String): String {
        val id = imageUrl.hashCode().toString()
        return getCachedAlbumArtUrlAsync(id, imageUrl).join()
    }

    fun cleanup() {
        performMaintenance()
        close()
    }

    private fun downloadAndCacheImage(id: String, imageUrl: String): String {
        val cacheFile = getCacheFile(id)
        return try {
            val image = ImageIO.read(URL(imageUrl))
                ?: throw java.io.IOException("Failed to read image from URL")
            val resizedImage = resizeImage(image)
            cacheFile.parentFile?.mkdirs()
            ImageIO.write(resizedImage, "png", cacheFile)
            cacheFile.absolutePath
        } catch (e: Exception) {
            ShindoLogger.error("Failed to download and cache album art: $id", e)
            imageUrl
        }
    }

    private fun resizeImage(original: BufferedImage): BufferedImage {
        val resultingImage = original.getScaledInstance(
            MAX_IMAGE_SIZE, MAX_IMAGE_SIZE,
            Image.SCALE_SMOOTH
        )
        val outputImage = BufferedImage(
            MAX_IMAGE_SIZE, MAX_IMAGE_SIZE,
            BufferedImage.TYPE_INT_ARGB
        )
        outputImage.graphics.drawImage(resultingImage, 0, 0, null)
        return outputImage
    }

    private fun getCacheFile(id: String): File = File(cacheDir, "$id.png")

    private fun isValidCacheFile(file: File): Boolean =
        file.exists() &&
            System.currentTimeMillis() - file.lastModified() < CACHE_DURATION.toMillis()

    private fun performMaintenance() {
        try {
            var totalSize = 0L
            val expiredFiles = mutableListOf<File>()
            val files = cacheDir.listFiles() ?: return

            for (file in files) {
                if (!isValidCacheFile(file)) {
                    expiredFiles.add(file)
                } else {
                    totalSize += file.length()
                }
            }

            for (file in expiredFiles) {
                if (!file.delete()) {
                    ShindoLogger.warn("Failed to delete expired cache file: ${file.name}")
                }
            }

            val maxBytes = MAX_CACHE_SIZE_MB * 1024 * 1024
            if (totalSize > maxBytes) {
                Arrays.sort(files) { a, b -> a.lastModified().compareTo(b.lastModified()) }
                for (file in files) {
                    if (totalSize <= maxBytes) break
                    if (file.delete()) {
                        totalSize -= file.length()
                    }
                }
            }
        } catch (e: Exception) {
            ShindoLogger.error("Error during cache maintenance", e)
        }
    }

    override fun close() {
        downloadExecutor.shutdownNow()
        maintenanceExecutor.shutdownNow()
        try {
            downloadExecutor.awaitTermination(5, TimeUnit.SECONDS)
            maintenanceExecutor.awaitTermination(5, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    companion object {
        private const val MAX_CACHE_SIZE_MB = 100
        private const val MAX_IMAGE_SIZE = 300
        private const val CACHE_DIR = "album_art_cache"
        private val CACHE_DURATION = Duration.ofDays(30)
        private const val MAX_CONCURRENT_DOWNLOADS = 3
    }
}
