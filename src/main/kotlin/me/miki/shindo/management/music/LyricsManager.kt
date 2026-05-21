package me.miki.shindo.management.music

import com.google.gson.Gson
import com.wrapper.spotify.model_objects.specification.Track
import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.mods.impl.MusicInfoMod
import me.miki.shindo.management.music.model.CachedLyrics
import me.miki.shindo.management.music.model.LyricsLine
import me.miki.shindo.management.music.model.LyricsResponse
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

@Suppress("UNUSED")
class LyricsManager {
    private val gson = Gson()
    private val executorService =
        Executors.newSingleThreadExecutor { r ->
            Thread(r).apply {
                name = "Lyrics-Fetcher"
                isDaemon = true
            }
        }
    private val lyricsCache = ConcurrentHashMap<String, CachedLyrics>()

    private var currentTrackId: String? = null
    private var currentLyrics: LyricsResponse? = null
    private var currentLineIndex: Int = 0

    fun fetchLyrics(track: Track?): CompletableFuture<LyricsResponse?> {
        if (track?.id == null) {
            reset()
            return CompletableFuture.completedFuture(null)
        }

        val trackId = track.id

        if (trackId != currentTrackId) {
            reset()
        }

        val cached = lyricsCache[trackId]
        if (cached != null && !cached.isExpired()) {
            currentTrackId = trackId
            currentLyrics = cached.lyrics
            currentLineIndex = 0
            return CompletableFuture.completedFuture(cached.lyrics)
        }

        val apiUrl =
            getLyricsApiUrl(trackId)
                ?: run {
                    ShindoLogger.error("Failed to construct API URL for track: ${track.name}")
                    return CompletableFuture.completedFuture(null)
                }

        return CompletableFuture.supplyAsync(
            Supplier<LyricsResponse?> supplyAsync@{
                try {
                    val connection = URL(apiUrl).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = TIMEOUT_MS
                    connection.readTimeout = TIMEOUT_MS
                    connection.setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
                    )

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                            val response = reader.readText()
                            val lyrics = gson.fromJson(response, LyricsResponse::class.java)
                            if (lyrics != null && !lyrics.isError() && lyrics.lines.isNotEmpty()) {
                                lyricsCache[trackId] = CachedLyrics(lyrics)
                                currentTrackId = trackId
                                currentLyrics = lyrics
                                currentLineIndex = 0
                                return@supplyAsync lyrics
                            }
                        }
                        reset()
                    } else {
                        ShindoLogger.info("Failed to get lyrics, HTTP response code: ${connection.responseCode}")
                        reset()
                    }
                    return@supplyAsync null
                } catch (e: Exception) {
                    ShindoLogger.error("Error fetching lyrics: ${e.message}")
                    reset()
                    return@supplyAsync null
                }
            },
            executorService,
        )
    }

    private fun getLyricsApiUrl(trackId: String?): String? {
        if (trackId.isNullOrEmpty()) return null

        return try {
            val musicInfoMod = MusicInfoMod.instance ?: return defaultUrl(trackId)
            var baseUrl = musicInfoMod.getLyricsApiUrlSetting()?.getText()?.trim() ?: DEFAULT_LYRICS_API_URL

            if (baseUrl.contains("trackid=$trackId")) return baseUrl

            while (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length - 1)
            }

            val afterProtocol = baseUrl.indexOf("://") + 3
            val hasPath = baseUrl.substring(afterProtocol).contains("/")
            val hasParams = baseUrl.contains("?")

            when {
                hasParams -> baseUrl + (if (baseUrl.endsWith("&")) "" else "&") + "trackid=$trackId"
                !hasPath -> "$baseUrl/?trackid=$trackId"
                else -> "$baseUrl?trackid=$trackId"
            }
        } catch (e: Exception) {
            ShindoLogger.error("Error formatting lyrics API URL: ${e.message}")
            defaultUrl(trackId)
        }
    }

    private fun defaultUrl(trackId: String): String = "$DEFAULT_LYRICS_API_URL?trackid=$trackId"

    fun reset() {
        currentTrackId = null
        currentLyrics = null
        currentLineIndex = 0
    }

    fun updateCurrentLineIndex(currentPositionMs: Long) {
        val lines = currentLyrics?.lines ?: return
        if (lines.isEmpty()) return

        var newIndex = 0
        for (i in lines.indices) {
            if (lines[i].startTime <= currentPositionMs) {
                newIndex = i
            } else {
                break
            }
        }
        currentLineIndex = newIndex
    }

    fun getCurrentLyrics(): LyricsResponse? = currentLyrics

    fun getCurrentLineIndex(): Int = currentLineIndex

    fun getVisibleLines(totalLines: Int): List<LyricsLine> {
        val lines = currentLyrics?.lines ?: return emptyList()
        if (lines.isEmpty()) return emptyList()

        val halfLines = totalLines / 2
        val startIndex = (currentLineIndex - halfLines).coerceAtLeast(0)
        val visible = mutableListOf<LyricsLine>()
        for (i in 0 until totalLines) {
            val index = startIndex + i
            if (index < lines.size) {
                visible.add(lines[index])
            } else {
                break
            }
        }
        return visible
    }

    fun clearCache() {
        lyricsCache.clear()
    }

    fun shutdown() {
        executorService.shutdown()
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executorService.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }

    fun processLyricsRomanization(lyrics: LyricsResponse?) {
        if (lyrics == null || lyrics.isError() || lyrics.lines.isEmpty()) return

        val romanizer = Shindo.getInstance().getRomanizationManager()

        val linesToProcess =
            lyrics.lines.filter { line ->
                !line.words.isNullOrEmpty() && romanizer.containsJapaneseCharacters(line.words)
            }

        if (linesToProcess.isEmpty()) return

        val chunkSize = 10
        for (i in linesToProcess.indices step chunkSize) {
            val chunk = linesToProcess.subList(i, minOf(i + chunkSize, linesToProcess.size))
            for (line in chunk) {
                romanizer.romanizeText(line.words)!!.thenAccept { romanized ->
                    if (!romanized.isNullOrEmpty()) {
                        line.romanizedWords = romanized
                    }
                }
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_LYRICS_API_URL = "https://spotify.mopigames.gay/"
        private const val TIMEOUT_SECONDS = 10
        private const val TIMEOUT_MS = TIMEOUT_SECONDS * 1000
    }
}
