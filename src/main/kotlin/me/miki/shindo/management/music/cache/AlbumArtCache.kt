package me.miki.shindo.management.music.cache

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.file.FileManager
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import javax.imageio.ImageIO

@Suppress("UNUSED")
class AlbumArtCache(
    private val fileManager: FileManager,
) : AutoCloseable {
    private val cacheDir: File = File(fileManager.musicDir, CACHE_DIR)
    private val inProgressDownloads = ConcurrentHashMap<String, CompletableFuture<String>>()
    private val downloadExecutor =
        ThreadPoolExecutor(
            2,
            MAX_CONCURRENT_DOWNLOADS,
            60L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            object : ThreadFactory {
                private val defaultFactory = Executors.defaultThreadFactory()

                override fun newThread(r: Runnable): Thread {
                    val thread = defaultFactory.newThread(r)
                    thread.isDaemon = true
                    thread.name = "AlbumArt-Download-${thread.id}"
                    return thread
                }
            },
        )
    private val maintenanceExecutor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r).apply {
                name = "AlbumArt-Maintenance"
                isDaemon = true
            }
        }

    private var cachedPlaceholder: BufferedImage? = null

    init {
        initializeCache()
        scheduleMaintenance()
        preloadPlaceholder()
    }

    private fun preloadPlaceholder() {
        try {
            cachedPlaceholder = createDefaultPlaceholder()
        } catch (e: Exception) {
            ShindoLogger.warn("Could not preload placeholder: ${e.message}")
        }
    }

    private fun createDefaultPlaceholder(): BufferedImage {
        val img = BufferedImage(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB)
        val g = img.graphics
        g.color = java.awt.Color(40, 40, 40)
        g.fillRect(0, 0, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
        g.color = java.awt.Color(80, 80, 80)
        val centerX = MAX_IMAGE_SIZE / 2
        val centerY = MAX_IMAGE_SIZE / 2
        g.fillOval(centerX - 30, centerY - 30, 60, 60)
        g.color = java.awt.Color(60, 60, 60)
        g.fillOval(centerX - 20, centerY - 20, 40, 40)
        g.dispose()
        return img
    }

    private fun initializeCache() {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            ShindoLogger.error("Failed to create album art cache directory")
        }
    }

    private fun scheduleMaintenance() {
        maintenanceExecutor.scheduleAtFixedRate(
            this::performMaintenance,
            1,
            24,
            TimeUnit.HOURS,
        )
    }

    fun getCachedAlbumArtUrlAsync(
        id: String?,
        imageUrl: String?,
    ): CompletableFuture<String> {
        if (imageUrl.isNullOrBlank()) {
            return CompletableFuture.completedFuture(PLACEHOLDER_PATH)
        }
        return inProgressDownloads.computeIfAbsent(id!!) { key: String? ->
            val cachedFile = getCacheFile(id)
            if (cachedFile.exists() && isValidCacheFile(cachedFile)) {
                return@computeIfAbsent CompletableFuture.completedFuture(cachedFile.absolutePath)
            }
            CompletableFuture
                .supplyAsync(
                    Supplier { downloadAndCacheImage(id, imageUrl) },
                    downloadExecutor,
                ).whenComplete { result: String?, ex: Throwable? ->
                }
        }
    }

    fun getAlbumArt(imageUrl: String): String {
        val id = imageUrl.hashCode().toString()
        val cachedFile = getCacheFile(id)

        if (cachedFile.exists() && isValidCacheFile(cachedFile)) {
            return cachedFile.absolutePath
        }

        getCachedAlbumArtUrlAsync(id, imageUrl)
        return PLACEHOLDER_PATH
    }

    fun getAlbumArtAsync(imageUrl: String): CompletableFuture<String> {
        val id = imageUrl.hashCode().toString()
        return getCachedAlbumArtUrlAsync(id, imageUrl)
    }

    fun isImageReady(imageUrl: String): Boolean {
        val id = imageUrl.hashCode().toString()
        val cachedFile = getCacheFile(id)
        return cachedFile.exists() && isValidCacheFile(cachedFile)
    }

    fun cleanup() {
        performMaintenance()
        close()
    }

    private fun downloadAndCacheImage(
        id: String,
        imageUrl: String,
    ): String {
        val cacheFile = getCacheFile(id)
        return try {
            val connection = URL(imageUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.setRequestProperty("User-Agent", USER_AGENT)

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw java.io.IOException("HTTP error: $responseCode")
            }

            connection.inputStream.use { inputStream ->
                val image =
                    ImageIO.read(inputStream)
                        ?: throw java.io.IOException("Failed to decode image")

                val resizedImage = resizeImage(image)
                cacheFile.parentFile?.mkdirs()
                ImageIO.write(resizedImage, "png", cacheFile)
            }

            ShindoLogger.info("Cached album art: $id")
            cacheFile.absolutePath
        } catch (e: Exception) {
            ShindoLogger.warn("Failed to download album art: $id (${e.message})")
            PLACEHOLDER_PATH
        }
    }

    private fun resizeImage(original: BufferedImage): BufferedImage {
        val resultingImage =
            original.getScaledInstance(
                MAX_IMAGE_SIZE,
                MAX_IMAGE_SIZE,
                Image.SCALE_SMOOTH,
            )
        val outputImage =
            BufferedImage(
                MAX_IMAGE_SIZE,
                MAX_IMAGE_SIZE,
                BufferedImage.TYPE_INT_ARGB,
            )
        outputImage.graphics.apply {
            drawImage(resultingImage, 0, 0, null)
            dispose()
        }
        return outputImage
    }

    private fun getCacheFile(id: String): File = File(cacheDir, "$id.png")

    private fun isValidCacheFile(file: File): Boolean =
        file.exists() &&
            System.currentTimeMillis() - file.lastModified() < CACHE_DURATION.toMillis()

    private fun performMaintenance() {
        try {
            val files = cacheDir.listFiles() ?: return
            var totalSize = 0L
            val expiredFiles = mutableListOf<File>()
            val validFiles = mutableListOf<File>()

            for (file in files) {
                if (isValidCacheFile(file)) {
                    validFiles.add(file)
                    totalSize += file.length()
                } else {
                    expiredFiles.add(file)
                }
            }

            for (file in expiredFiles) {
                if (!file.delete()) {
                    ShindoLogger.warn("Failed to delete expired cache file: ${file.name}")
                }
            }

            val maxBytes = MAX_CACHE_SIZE_MB * 1024L * 1024L
            if (totalSize > maxBytes) {
                validFiles.sortBy { it.lastModified() }
                for (file in validFiles) {
                    if (totalSize <= maxBytes) break
                    if (file.delete()) {
                        totalSize -= file.length()
                    }
                }
                ShindoLogger.info("Cache cleanup complete. Size: ${totalSize / 1024 / 1024}MB")
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
        private const val MAX_CONCURRENT_DOWNLOADS = 4
        private const val CONNECT_TIMEOUT_MS = 8000
        private const val READ_TIMEOUT_MS = 15000
        private const val USER_AGENT = "Shindo/1.0"

        const val PLACEHOLDER_PATH = "__PLACEHOLDER__"
    }
}
