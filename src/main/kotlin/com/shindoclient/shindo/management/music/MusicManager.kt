package com.shindoclient.shindo.management.music

import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import com.shindoclient.spotify.Spotify
import com.shindoclient.spotify.data.PlaylistSimplified
import com.shindoclient.spotify.data.Track
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.logger.ShindoLogger.error
import com.shindoclient.shindo.management.file.FileManager
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.music.cache.AlbumArtCache
import com.shindoclient.shindo.management.notification.NotificationType
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.URL
import java.util.Properties
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MusicManager(
    private val fileManager: FileManager,
) : AutoCloseable {
    private val albumArtCache = AlbumArtCache(fileManager)
    private val lyricsManager = LyricsManager()
    private val searchCache = ConcurrentHashMap<String, CompletableFuture<List<Track>>>()
    private val playlistCache = ConcurrentHashMap<String, CompletableFuture<List<PlaylistSimplified>>>()

    private val spotify: Spotify = Spotify(
        clientId = "",
        clientSecret = "",
        redirectUri = "http://127.0.0.1:8888/callback",
    )
    private var scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var server: HttpServer? = null
    private var isAuthorized: Boolean = false
    private var currentTrack: Track? = null
    private var isPlaying: Boolean = false
    private var currentVolume: Int = 100
    private var trackPosition: Long = 0
    private var trackDuration: Long = 0
    private var tokenRefreshScheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var lastPositionUpdateTime: Long = 0
    private var trackInfoCallback: TrackInfoCallback? = null

    init {
        initializeSchedulers()
        loadTokens()
        if (spotify.api.accessToken != null) {
            isAuthorized = true
            startPlaybackStateUpdater()
            scheduleTokenRefresh()
        } else {
            try {
                startServer()
            } catch (e: IOException) {
                ShindoLogger.error("Failed to start local callback server", e)
            }
        }
        Runtime.getRuntime().addShutdownHook(Thread { cleanup() })
    }

    private fun initializeSchedulers() {
        scheduler =
            Executors.newSingleThreadScheduledExecutor { r ->
                Thread(r).apply { isDaemon = true }
            }
        tokenRefreshScheduler =
            Executors.newSingleThreadScheduledExecutor { r ->
                Thread(r).apply { isDaemon = true }
            }
    }

    private fun loadTokens() {
        val tokenFile = File(fileManager.musicDir, TOKEN_FILE_NAME)
        val props = Properties()
        try {
            tokenFile.inputStream().use { props.load(it) }
            val accessToken = props.getProperty("accessToken")
            val refreshToken = props.getProperty("refreshToken")
            if (accessToken != null && refreshToken != null) {
                spotify.api.accessToken = accessToken
                spotify.api.refreshToken = refreshToken
                refreshAccessToken()
            }
        } catch (e: IOException) {
            ShindoLogger.warn("Failed to load tokens: ${e.message}")
        }
    }

    private fun saveTokens() {
        if (spotify.api.accessToken.isNullOrEmpty() || spotify.api.refreshToken.isNullOrEmpty()) return
        val tokenFile = File(fileManager.musicDir, TOKEN_FILE_NAME)
        val props =
            Properties().apply {
                setProperty("accessToken", spotify.api.accessToken)
                setProperty("refreshToken", spotify.api.refreshToken)
            }
        try {
            tokenFile.outputStream().use { props.store(it, "Spotify Tokens") }
        } catch (e: IOException) {
            ShindoLogger.error("Failed to save tokens", e)
            Shindo.getInstance().getNotificationManager().post(
                TranslateText.SPOTIFY_AUTH,
                TranslateText.SPOTIFY_FAILED_TO_SAVE_TOKENS,
                NotificationType.ERROR,
            )
        }
    }

    @Throws(IOException::class)
    private fun startServer() {
        server =
            HttpServer.create(InetSocketAddress(8888), 0).apply {
                createContext("/callback", SpotifyCallbackHandler())
                executor =
                    Executors.newSingleThreadExecutor { r ->
                        Thread(r).apply { isDaemon = true }
                    }
                start()
            }
    }

    fun getAuthorizationCodeUri(): String = "$CDN_BASE_URL/api/spotify/login"

    private fun requestAccessToken(code: String) {
        try {
            val url = URL("$CDN_BASE_URL/spotify/token?code=$code")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val json = JsonParser.parseString(response).asJsonObject
                spotify.api.accessToken = json.get("access_token").asString
                spotify.api.refreshToken = json.get("refresh_token")?.asString
                isAuthorized = true
                saveTokens()
                ShindoLogger.info("Successfully obtained Spotify access token via CDN proxy")
            } else {
                error("CDN token exchange failed: ${connection.responseCode}")
            }
        } catch (e: Exception) {
            error("Failed to exchange code for token: ${e.message}")
        }
    }

    fun isAuthorized(): Boolean = isAuthorized

    fun hasCredentials(): Boolean = true

    fun searchTracks(query: String?): CompletableFuture<List<Track>> {
        return searchCache.computeIfAbsent(
            query!!,
        ) { q: String? ->
            spotify.search.searchTracks(q!!, SEARCH_LIMIT)
                .thenApply { tracks ->
                    CompletableFuture.runAsync {
                        tracks.chunked(BATCH_SIZE).forEach { batch ->
                            batch.forEach { prefetchAlbumArt(it) }
                            Thread.sleep(THROTTLE_DELAY)
                        }
                    }
                    tracks
                }
                .whenComplete { _, _ -> searchCache.remove(query) }
        }
    }

    fun searchPlaylists(query: String): CompletableFuture<List<PlaylistSimplified>> {
        return playlistCache.computeIfAbsent(
            "search:$query",
        ) { q: String? ->
            spotify.search.searchPlaylists(query, SEARCH_LIMIT)
                .thenApply { playlists ->
                    CompletableFuture.runAsync {
                        playlists.chunked(BATCH_SIZE).forEach { batch ->
                            batch.forEach { getPlaylistImageUrl(it) }
                            Thread.sleep(THROTTLE_DELAY)
                        }
                    }
                    playlists
                }
                .whenComplete { _, _ -> playlistCache.remove("search:$query") }
        }
    }

    private fun prefetchAlbumArt(track: Track?) {
        val images = track?.album?.images
        if (images.isNullOrEmpty()) return
        val imageUrl = images[0].url ?: return
        try {
            albumArtCache
                .getCachedAlbumArtUrlAsync(track.id, imageUrl)
                .exceptionally { ex ->
                    ShindoLogger.warn("Failed to prefetch album art: ${ex.message}")
                    imageUrl
                }
        } catch (e: Exception) {
            ShindoLogger.warn("Error during album art prefetch: ${e.message}")
        }
    }

    fun addToQueue(trackUri: String): CompletableFuture<Void> =
        spotify.player.addToQueue(trackUri)
            .thenRun { fetchCurrentPlaybackState() }
            .exceptionally { e ->
                if (e is CompletionException) {
                    ShindoLogger.error("Failed to add track to queue", e.cause ?: e)
                }
                null
            }

    fun play(trackUri: String) {
        CompletableFuture.runAsync {
            try {
                val deviceId = getActiveDeviceId()
                fetchCurrentPlaybackState()
                if (deviceId == null) {
                    Shindo.getInstance().getNotificationManager().post(
                        TranslateText.SPOTIFY_PLAYBACK,
                        TranslateText.SPOTIFY_NO_ACTIVE_DEVICE,
                        NotificationType.ERROR,
                    )
                    return@runAsync
                }
                spotify.player.startResumePlayback(
                    deviceId = deviceId,
                    uris = listOf(trackUri),
                ).join()
                isPlaying = true
                updatePlaybackState()
                Shindo.getInstance().getNotificationManager().post(
                    TranslateText.SPOTIFY_PLAYBACK,
                    TranslateText.SPOTIFY_PLAYBACK_STARTED,
                    NotificationType.SUCCESS,
                )
            } catch (e: Exception) {
                val cause = if (e is CompletionException) e.cause else e
                if (cause?.message?.contains("Restriction violated") == true) {
                    ShindoLogger.warn(
                        "Play command restricted - likely due to Spotify Premium requirement or device limitations",
                    )
                    Shindo.getInstance().getNotificationManager().post(
                        TranslateText.SPOTIFY_PLAYBACK,
                        TranslateText.SPOTIFY_PLAYBACK_RESTRICTED,
                        NotificationType.WARNING,
                    )
                    fetchCurrentPlaybackState()
                } else {
                    handleSpotifyException("start playback", cause ?: e)
                }
            }
        }
    }

    fun pause() {
        if (!isPlaying) return
        CompletableFuture.runAsync {
            try {
                spotify.player.pausePlayback().join()
                fetchCurrentPlaybackState()
                isPlaying = false
            } catch (e: Exception) {
                handleSpotifyException("pause playback", if (e is CompletionException) (e.cause ?: e) else e)
            }
        }
    }

    fun resume() {
        if (isPlaying) return
        CompletableFuture.runAsync {
            try {
                val deviceId = getActiveDeviceId()
                if (deviceId == null) {
                    Shindo.getInstance().getNotificationManager().post(
                        TranslateText.SPOTIFY_PLAYBACK,
                        TranslateText.SPOTIFY_NO_ACTIVE_DEVICE,
                        NotificationType.ERROR,
                    )
                    return@runAsync
                }
                spotify.player.startResumePlayback(deviceId = deviceId).join()
                fetchCurrentPlaybackState()
                isPlaying = true
            } catch (e: Exception) {
                val cause = if (e is CompletionException) e.cause else e
                if (cause?.message?.contains("Restriction violated") == true) {
                    ShindoLogger.warn(
                        "Resume playback restricted - likely due to Spotify Premium requirement or device limitations",
                    )
                    Shindo.getInstance().getNotificationManager().post(
                        TranslateText.SPOTIFY_PLAYBACK,
                        TranslateText.SPOTIFY_PREMIUM_REQUIRED,
                        NotificationType.WARNING,
                    )
                    fetchCurrentPlaybackState()
                } else {
                    handleSpotifyException("resume playback", cause ?: e)
                }
            }
        }
    }

    fun fetchAndUpdateVolume() {
        try {
            val state = spotify.player.getPlaybackState().get()
            if (state != null) {
                currentVolume = state.volumePercent
            }
        } catch (e: Exception) {
            ShindoLogger.warn("Error fetching current volume: ${e.message}")
        }
    }

    fun getVolume(): Int {
        try {
            if (!isPlaying && currentVolume == 100) {
                fetchAndUpdateVolume()
            }
        } catch (_: Exception) {
        }
        return currentVolume
    }

    fun setVolume(volumePercent: Int) {
        if (volumePercent == currentVolume) return
        CompletableFuture.runAsync {
            try {
                val state = spotify.player.getPlaybackState().get()
                if (state != null) {
                    currentVolume = state.volumePercent
                    if (volumePercent == currentVolume) return@runAsync
                }
                spotify.player.setVolume(volumePercent).join()
                currentVolume = volumePercent
            } catch (e: Exception) {
                handleSpotifyException("set volume", if (e is CompletionException) (e.cause ?: e) else e)
            }
        }
    }

    fun nextTrack() {
        CompletableFuture.runAsync {
            try {
                spotify.player.nextTrack().join()
                fetchCurrentPlaybackState()
                updatePlaybackState()
            } catch (e: Exception) {
                handleSpotifyException("skip to next track", if (e is CompletionException) (e.cause ?: e) else e)
            }
        }
    }

    fun previousTrack() {
        CompletableFuture.runAsync {
            try {
                spotify.player.previousTrack().join()
                fetchCurrentPlaybackState()
                updatePlaybackState()
            } catch (e: Exception) {
                handleSpotifyException("skip to previous track", if (e is CompletionException) (e.cause ?: e) else e)
            }
        }
    }

    fun seekToPosition(positionMs: Long) {
        CompletableFuture.runAsync {
            try {
                spotify.player.seekTo(positionMs.toInt()).join()
                synchronized(this@MusicManager) {
                    trackPosition = positionMs
                    lastPositionUpdateTime = System.currentTimeMillis()
                    notifyTrackInfoUpdated()
                }
                scheduler.schedule({ synchronizePlaybackPosition() }, 300, TimeUnit.MILLISECONDS)
            } catch (e: Exception) {
                handleSpotifyException("seek to position", if (e is CompletionException) (e.cause ?: e) else e)
            }
        }
    }

    private fun updatePlaybackState() {
        try {
            val currentlyPlaying = spotify.player.getCurrentlyPlaying().get()
            if (currentlyPlaying?.currentTrack != null) {
                currentTrack = currentlyPlaying.currentTrack
                isPlaying = currentlyPlaying.isPlaying
                trackPosition = currentlyPlaying.progressMs
                trackDuration = currentTrack!!.durationMs.toLong()
                notifyTrackInfoUpdated()
            }
        } catch (e: Exception) {
            ShindoLogger.error("Error updating playback state", e)
        }
    }

    private fun handleSpotifyException(
        action: String,
        e: Throwable,
    ) {
        ShindoLogger.error("Failed to $action: ${e.message}", e)
        val errorText =
            when (action) {
                "start playback" -> TranslateText.SPOTIFY_PLAYBACK_START_FAILED
                "pause playback" -> TranslateText.SPOTIFY_PLAYBACK_PAUSE_FAILED
                "resume playback" -> TranslateText.SPOTIFY_PLAYBACK_RESUME_FAILED
                "set volume" -> TranslateText.SPOTIFY_VOLUME_SET_FAILED
                "play playlist" -> TranslateText.SPOTIFY_FAILED_TO_PLAY_PLAYLIST
                else -> TranslateText.ERROR
            }
        Shindo.getInstance().getNotificationManager().post(
            TranslateText.SPOTIFY_PLAYBACK,
            errorText,
            NotificationType.ERROR,
        )
    }

    private fun getActiveDeviceId(): String? {
        try {
            val devices = spotify.player.getAvailableDevices().get()
            if (devices.isEmpty()) {
                ShindoLogger.warn("No Spotify devices found")
                return null
            }

            devices.firstOrNull { it.isActive }?.let {
                return it.id
            }

            devices.firstOrNull()?.let {
                ShindoLogger.info("No active device found, using first available: ${it.name}")
                return it.id
            }

            ShindoLogger.warn("No active device found")
        } catch (e: Exception) {
            ShindoLogger.error("Failed to get active device", e)
        }
        return null
    }

    fun getAlbumArtUrl(track: Track?): String? {
        val images = track?.album?.images ?: return null
        if (images.isEmpty()) return null
        val imageUrl = images[0].url ?: return null

        return try {
            albumArtCache.getAlbumArt(imageUrl)
        } catch (e: Exception) {
            AlbumArtCache.PLACEHOLDER_PATH
        }
    }

    fun getAlbumArt(url: String): String = albumArtCache.getAlbumArt(url)

    private fun fetchCurrentPlaybackState() {
        try {
            val playbackState = spotify.player.getPlaybackState().get() ?: return
            isPlaying = playbackState.isPlaying
            trackPosition = playbackState.progressMs
            lastPositionUpdateTime = System.currentTimeMillis()
            playbackState.device?.let { currentVolume = it.volumePercent ?: currentVolume }
            val item = playbackState.currentTrack
            if (item != null) {
                if (currentTrack == null || currentTrack!!.id != item.id) {
                    currentTrack = item
                    trackDuration = item.durationMs.toLong()
                }
            }
            notifyTrackInfoUpdated()
        } catch (e: Exception) {
            ShindoLogger.error("Error fetching playback state", e)
        }
    }

    fun getCurrentTrack(): Track? = currentTrack

    fun isPlaying(): Boolean = isPlaying

    private fun startPlaybackStateUpdater() {
        scheduler.scheduleAtFixedRate({
            if (isAuthorized) {
                fetchCurrentPlaybackState()
            }
        }, 0, PLAYBACK_UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
    }

    fun getCurrentPosition(): Long {
        if (!isPlaying) return trackPosition
        val now = System.currentTimeMillis()
        val elapsed = if (lastPositionUpdateTime > 0) now - lastPositionUpdateTime else 0L
        if (elapsed > 3000) {
            synchronizePlaybackPosition()
            return trackPosition
        }
        return minOf(trackPosition + elapsed, trackDuration)
    }

    fun synchronizePlaybackPosition(): CompletableFuture<Void> =
        CompletableFuture.runAsync {
            try {
                val playbackState = spotify.player.getPlaybackState().get()
                if (playbackState != null) {
                    synchronized(this@MusicManager) {
                        isPlaying = playbackState.isPlaying
                        trackPosition = playbackState.progressMs
                        lastPositionUpdateTime = System.currentTimeMillis()
                        val item = playbackState.currentTrack
                        if (item != null) {
                            if (currentTrack == null || currentTrack!!.id != item.id) {
                                currentTrack = item
                                trackDuration = item.durationMs.toLong()
                            }
                        }
                        notifyTrackInfoUpdated()
                    }
                }
            } catch (e: Exception) {
                ShindoLogger.error("Error during position sync: ${e.message}", e)
            }
        }

    fun getCurrentTime(): Float = getCurrentPosition() / 1000f

    fun getEndTime(): Float = trackDuration / 1000f

    private fun notifyTrackInfoUpdated() {
        trackInfoCallback?.onTrackInfoUpdated(getCurrentPosition(), trackDuration)
    }

    fun setTrackInfoCallback(callback: TrackInfoCallback?) {
        trackInfoCallback = callback
    }

    fun refreshAccessToken() {
        val refreshTokenValue = spotify.api.refreshToken ?: run {
            ShindoLogger.warn("No refresh token available")
            return
        }
        try {
            val encoded = java.net.URLEncoder.encode(refreshTokenValue, "UTF-8")
            val connection =
                URL("$CDN_BASE_URL/api/spotify/refresh?refresh_token=$encoded")
                    .openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode == 200) {
                val json = JsonParser.parseString(connection.inputStream.bufferedReader().readText()).asJsonObject
                spotify.api.accessToken = json.get("access_token").asString
                json.get("refresh_token")?.asString?.let { spotify.api.refreshToken = it }
                saveTokens()
            } else {
                ShindoLogger.error("CDN token refresh failed: ${connection.responseCode}")
                isAuthorized = false
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to refresh access token: ${e.message}")
            isAuthorized = false
        }
    }

    private fun scheduleTokenRefresh() {
        val refreshInterval = 3600L - 300
        tokenRefreshScheduler.scheduleAtFixedRate(
            this::refreshAccessToken,
            refreshInterval,
            refreshInterval,
            TimeUnit.SECONDS,
        )
    }

    fun cleanup() {
        searchCache.clear()
        playlistCache.clear()
        albumArtCache.cleanup()
        lyricsManager.shutdown()
        server?.stop(0)
        scheduler.shutdownNow()
        tokenRefreshScheduler.shutdownNow()
        saveTokens()
    }

    override fun close() {
        cleanup()
        lyricsManager.shutdown()
    }

    fun getUserPlaylists(): CompletableFuture<List<PlaylistSimplified>> {
        val cacheKey = "userPlaylists"
        return playlistCache.computeIfAbsent(
            cacheKey,
        ) { k: String? ->
            spotify.user.getCurrentUserPlaylists(PLAYLIST_LIMIT)
                .thenApply { playlists ->
                    CompletableFuture.runAsync { prefetchPlaylistImages(playlists) }
                    playlists
                }
                .whenComplete { result, ex ->
                    if (ex != null) {
                        playlistCache.remove(cacheKey)
                    }
                }
        }
    }

    private fun prefetchPlaylistImages(playlists: List<PlaylistSimplified>) {
        try {
            playlists.chunked(BATCH_SIZE).forEach { batch ->
                batch
                    .filter { it.images.isNotEmpty() }
                    .forEach { getPlaylistImageUrl(it) }
                Thread.sleep(THROTTLE_DELAY)
            }
        } catch (e: InterruptedException) {
            ShindoLogger.error("[MUSIC] Failed to prefetch Playlist Images.", e)
            Thread.currentThread().interrupt()
        }
    }

    fun playPlaylist(playlistUri: String) {
        CompletableFuture.runAsync {
            try {
                spotify.player.startResumePlayback(contextUri = playlistUri).join()
            } catch (e: Exception) {
                ShindoLogger.error("Failed to play playlist", e)
                handleSpotifyException("play playlist", if (e is CompletionException) (e.cause ?: e) else e)
            }
        }
    }

    fun getPlaylistImageUrl(playlist: PlaylistSimplified?): String? {
        val images = playlist?.images ?: return null
        if (images.isEmpty()) return null
        val imageUrl = images[0].url ?: return null

        return try {
            albumArtCache.getAlbumArt(imageUrl)
        } catch (e: Exception) {
            ShindoLogger.warn("Album art error: ${e.message}")
            AlbumArtCache.PLACEHOLDER_PATH
        }
    }

    fun getLyricsManager(): LyricsManager = lyricsManager

    fun getTrackPosition(): Long = trackPosition

    /** Internal access for extension functions. */
    fun getSpotify(): Spotify = spotify

    private inner class SpotifyCallbackHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            val response = "Authorization successful! You can close this window now."
            exchange.sendResponseHeaders(200, response.length.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }

            val query = exchange.requestURI.query ?: return
            val params =
                query.split("&").associate {
                    val parts = it.split("=", limit = 2)
                    parts[0] to java.net.URLDecoder.decode(parts.getOrElse(1) { "" }, "UTF-8")
                }

            if (params.containsKey("error")) {
                ShindoLogger.error("Spotify auth error: ${params["error"]}")
                return
            }

            val accessToken = params["access_token"]
            val refreshToken = params["refresh_token"]

            if (!accessToken.isNullOrEmpty()) {
                spotify.api.accessToken = accessToken
                spotify.api.refreshToken = refreshToken
                isAuthorized = true
                saveTokens()
                startPlaybackStateUpdater()
                scheduleTokenRefresh()
                ShindoLogger.info("Spotify authorization successful via CDN proxy")
            } else {
                ShindoLogger.error("Callback received but no access_token found")
            }
        }
    }

    companion object {
        private const val CDN_BASE_URL = "https://cdn.shindoclient.com"
        private const val TOKEN_FILE_NAME = "spotify_tokens.properties"
        private const val CREDENTIALS_FILE_NAME = "spotify_credentials.properties"
        private const val SEARCH_LIMIT = 30
        private const val PLAYLIST_LIMIT = 50
        private const val PLAYBACK_UPDATE_INTERVAL = 1000L
        private const val BATCH_SIZE = 20
        private const val THROTTLE_DELAY = 50L
    }
}
