package me.miki.shindo.management.music

import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import me.miki.shindo.Shindo
import me.miki.shindo.libs.spotify.SpotifyApi
import me.miki.shindo.libs.spotify.SpotifyHttpManager
import me.miki.shindo.libs.spotify.exceptions.SpotifyWebApiException
import me.miki.shindo.libs.spotify.model_objects.credentials.AuthorizationCodeCredentials
import me.miki.shindo.libs.spotify.model_objects.miscellaneous.CurrentlyPlaying
import me.miki.shindo.libs.spotify.model_objects.miscellaneous.CurrentlyPlayingContext
import me.miki.shindo.libs.spotify.model_objects.miscellaneous.Device
import me.miki.shindo.libs.spotify.model_objects.specification.PlaylistSimplified
import me.miki.shindo.libs.spotify.model_objects.specification.Track
import me.miki.shindo.libs.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest
import me.miki.shindo.libs.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest
import me.miki.shindo.libs.spotify.requests.data.player.AddItemToUsersPlaybackQueueRequest
import me.miki.shindo.libs.spotify.requests.data.player.PauseUsersPlaybackRequest
import me.miki.shindo.libs.spotify.requests.data.player.StartResumeUsersPlaybackRequest
import me.miki.shindo.libs.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest
import me.miki.shindo.libs.spotify.requests.data.search.simplified.SearchPlaylistsRequest
import me.miki.shindo.libs.spotify.requests.data.search.simplified.SearchTracksRequest
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.logger.ShindoLogger.error
import me.miki.shindo.management.file.FileManager
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.music.cache.AlbumArtCache
import me.miki.shindo.management.notification.NotificationType
import org.apache.hc.core5.http.ParseException
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.URI
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Properties
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Supplier
class MusicManager(private val fileManager: FileManager) : AutoCloseable {

    private val albumArtCache = AlbumArtCache(fileManager)
    private val lyricsManager = LyricsManager()
    private val rateLimiter = SimpleRateLimiter(20.0)
    private val lastRequestTime = ConcurrentHashMap<String, Long>()
    private val searchCache = ConcurrentHashMap<String, CompletableFuture<List<Track>>>()
    private val playlistCache = ConcurrentHashMap<String, CompletableFuture<List<PlaylistSimplified>>>()

    private var clientId: String = ""
    private var clientSecret: String = ""
    private var hasCredentials: Boolean = false
    private lateinit var spotifyApi: SpotifyApi
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
        loadCredentials()

        if (hasCredentials) {
            spotifyApi = SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(REDIRECT_URI)
                .build()
            loadTokens()
            if (spotifyApi.accessToken != null) {
                isAuthorized = true
            } else {
                try {
                    startServer()
                } catch (e: IOException) {
                    ShindoLogger.error("Failed to start local server for Spotify authentication", e)
                    Shindo.getInstance().notificationManager.post(
                        TranslateText.SPOTIFY_AUTH,
                        TranslateText.SPOTIFY_FAIL_BROWSER,
                        NotificationType.ERROR
                    )
                }
            }
            startPlaybackStateUpdater()
            scheduleTokenRefresh()
        } else {
            spotifyApi = SpotifyApi.Builder().setRedirectUri(REDIRECT_URI).build()
        }

        Runtime.getRuntime().addShutdownHook(Thread { cleanup() })
    }

    private fun initializeSchedulers() {
        scheduler = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r).apply { isDaemon = true }
        }
        tokenRefreshScheduler = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r).apply { isDaemon = true }
        }
    }

    private fun loadCredentials() {
        clientId = "d94db01cef0743afa72e0869ffbb754d"
        clientSecret = "19e88ba57df44b06b4f4a0aaac02c8a9"
        hasCredentials = true
        ShindoLogger.info("Loaded Spotify credentials")
    }

    fun saveCredentials(clientId: String, clientSecret: String) {
        val credentialsFile = File(fileManager.musicDir, CREDENTIALS_FILE_NAME)
        val props = Properties().apply {
            setProperty("clientId", clientId)
            setProperty("clientSecret", clientSecret)
        }
        try {
            credentialsFile.outputStream().use { out -> props.store(out, "Spotify API Credentials") }
            this.clientId = clientId
            this.clientSecret = clientSecret
            this.hasCredentials = true
            spotifyApi = SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(REDIRECT_URI)
                .build()
            if (server == null) {
                try {
                    startServer()
                } catch (e: IOException) {
                    ShindoLogger.error("Failed to start local server for Spotify authentication", e)
                    Shindo.getInstance().notificationManager.post(
                        TranslateText.SPOTIFY_AUTH,
                        TranslateText.SPOTIFY_FAIL_BROWSER,
                        NotificationType.ERROR
                    )
                }
            }
            startPlaybackStateUpdater()
            scheduleTokenRefresh()
            ShindoLogger.info("Saved Spotify credentials")
        } catch (e: IOException) {
            ShindoLogger.error("Failed to save Spotify credentials", e)
            Shindo.getInstance().notificationManager.post(
                TranslateText.SPOTIFY_AUTH,
                TranslateText.SPOTIFY_FAILED_TO_SAVE_CREDENTIALS,
                NotificationType.ERROR
            )
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
                spotifyApi.accessToken = accessToken
                spotifyApi.refreshToken = refreshToken
                refreshAccessToken()
            }
        } catch (e: IOException) {
            ShindoLogger.warn("Failed to load tokens: ${e.message}")
        }
    }

    private fun saveTokens() {
        val tokenFile = File(fileManager.musicDir, TOKEN_FILE_NAME)
        val props = Properties().apply {
            setProperty("accessToken", spotifyApi.accessToken ?: "")
            setProperty("refreshToken", spotifyApi.refreshToken ?: "")
        }
        try {
            tokenFile.outputStream().use { props.store(it, "Spotify Tokens") }
        } catch (e: IOException) {
            ShindoLogger.error("Failed to save tokens", e)
            Shindo.getInstance().notificationManager.post(
                TranslateText.SPOTIFY_AUTH,
                TranslateText.SPOTIFY_FAILED_TO_SAVE_TOKENS,
                NotificationType.ERROR
            )
        }
    }

    @Throws(IOException::class)
    private fun startServer() {
        server = HttpServer.create(InetSocketAddress(8888), 0).apply {
            createContext("/callback", SpotifyCallbackHandler())
            executor = Executors.newSingleThreadExecutor { r ->
                Thread(r).apply { isDaemon = true }
            }
            start()
        }
    }

    fun getAuthorizationCodeUri(): String {
        val req = spotifyApi.authorizationCodeUri()
            .scope("user-read-private user-read-email user-modify-playback-state user-read-playback-state")
            .show_dialog(true)
            .build()
        return req.execute().toString()
    }

    private fun requestAccessToken(code: String) {
        try {
            val req = spotifyApi.authorizationCode(code).build()
            val creds = req.execute()
            spotifyApi.accessToken = creds.accessToken
            spotifyApi.refreshToken = creds.refreshToken
            isAuthorized = true
            saveTokens()
            Shindo.getInstance().notificationManager.post(
                TranslateText.SPOTIFY_AUTH,
                TranslateText.SPOTIFY_AUTH_TOKEN_RECEIVED,
                NotificationType.SUCCESS
            )
        } catch (e: Exception) {
            if (e is IOException || e is SpotifyWebApiException || e is ParseException) {
                ShindoLogger.error("Failed to request access token", e)
                Shindo.getInstance().notificationManager.post(
                    TranslateText.SPOTIFY_AUTH,
                    TranslateText.SPOTIFY_AUTH_FAILED,
                    NotificationType.ERROR
                )
            }
        }
    }

    fun isAuthorized(): Boolean = isAuthorized
    fun hasCredentials(): Boolean = hasCredentials

    fun searchTracks(query: String?): CompletableFuture<List<Track>> {
        return searchCache.computeIfAbsent(
            query!!
        ) { q: String? ->
            throttleRequest("search",
                Supplier {
                    CompletableFuture.supplyAsync {
                        try {
                            val request = spotifyApi.searchTracks(q)
                                .limit(SEARCH_LIMIT)
                                .build()
                            val tracks =
                                listOf(*request.execute().items)
                            CompletableFuture.runAsync {
                                var i = 0
                                while (i < tracks.size) {
                                    val end = (i + BATCH_SIZE).coerceAtMost(tracks.size)
                                    val batch: List<Track> =
                                        tracks.subList(i, end)
                                    batch.forEach(Consumer { track: Track? ->
                                        prefetchAlbumArt(
                                            track
                                        )
                                    })
                                    try {
                                        Thread.sleep(THROTTLE_DELAY)
                                    } catch (e: InterruptedException) {
                                        Thread.currentThread().interrupt()
                                    }
                                    i += BATCH_SIZE
                                }
                            }
                            return@supplyAsync tracks
                        } catch (e: java.lang.Exception) {
                            error("Search failed", e)
                            throw CompletionException(e)
                        } finally {
                            searchCache.remove(query)
                        }
                    }
                })
        }
    }

    fun searchPlaylists(query: String): CompletableFuture<List<PlaylistSimplified>> {
        return playlistCache.computeIfAbsent(
            "search:$query"
        ) { q: String? ->
            throttleRequest("search_playlist",
                Supplier {
                    CompletableFuture.supplyAsync {
                        try {
                            val request = spotifyApi.searchPlaylists(query)
                                .limit(SEARCH_LIMIT)
                                .build()
                            val playlists =
                                listOf(*request.execute().items)
                            CompletableFuture.runAsync {
                                var i = 0
                                while (i < playlists.size) {
                                    val end = (i + BATCH_SIZE).coerceAtMost(playlists.size)
                                    val batch: List<PlaylistSimplified> =
                                        playlists.subList(i, end)
                                    batch.forEach(Consumer { playlist: PlaylistSimplified? ->
                                        getPlaylistImageUrl(
                                            playlist
                                        )
                                    })
                                    try {
                                        Thread.sleep(THROTTLE_DELAY)
                                    } catch (e: InterruptedException) {
                                        Thread.currentThread().interrupt()
                                    }
                                    i += BATCH_SIZE
                                }
                            }
                            return@supplyAsync playlists
                        } catch (e: java.lang.Exception) {
                            error("Playlist search failed", e)
                            throw CompletionException(e)
                        } finally {
                            playlistCache.remove("search:$query")
                        }
                    }
                })
        }
    }

    private fun prefetchAlbumArt(track: Track?) {
        val images = track?.album?.images
        if (images == null || images.isEmpty()) return
        val imageUrl = images[0].url ?: return
        try {
            albumArtCache.getCachedAlbumArtUrlAsync(track.id, imageUrl)
                .exceptionally { ex ->
                    ShindoLogger.warn("Failed to prefetch album art: ${ex.message}")
                    imageUrl
                }
        } catch (e: Exception) {
            ShindoLogger.warn("Error during album art prefetch: ${e.message}")
        }
    }

    fun addToQueue(trackUri: String): CompletableFuture<Void> =
        CompletableFuture.runAsync {
            try {
                val deviceId = getActiveDeviceId()
                    ?: throw IllegalStateException("No active device found")
                val req = spotifyApi.addItemToUsersPlaybackQueue(trackUri)
                    .device_id(deviceId)
                    .build()
                req.execute()
                fetchCurrentPlaybackState()
            } catch (e: Exception) {
                if (e is IOException || e is SpotifyWebApiException || e is ParseException) {
                    ShindoLogger.error("Failed to add track to queue", e)
                    throw CompletionException(e)
                }
            }
        }

    fun play(trackUri: String) {
        CompletableFuture.runAsync {
            try {
                val deviceId = getActiveDeviceId()
                fetchCurrentPlaybackState()
                if (deviceId == null) {
                    Shindo.getInstance().notificationManager.post(
                        TranslateText.SPOTIFY_PLAYBACK,
                        TranslateText.SPOTIFY_NO_ACTIVE_DEVICE,
                        NotificationType.ERROR
                    )
                    return@runAsync
                }
                val uris = JsonParser.parseString("[\"$trackUri\"]").asJsonArray
                val playbackRequest = spotifyApi.startResumeUsersPlayback()
                    .device_id(deviceId)
                    .uris(uris)
                    .build()
                try {
                    playbackRequest.execute()
                    isPlaying = true
                    updatePlaybackState()
                    Shindo.getInstance().notificationManager.post(
                        TranslateText.SPOTIFY_PLAYBACK,
                        TranslateText.SPOTIFY_PLAYBACK_STARTED,
                        NotificationType.SUCCESS
                    )
                } catch (e: Exception) {
                    if (e.message?.contains("Restriction violated") == true) {
                        ShindoLogger.warn("Play command restricted - likely due to Spotify Premium requirement or device limitations")
                        Shindo.getInstance().notificationManager.post(
                            TranslateText.SPOTIFY_PLAYBACK,
                            TranslateText.SPOTIFY_PLAYBACK_RESTRICTED,
                            NotificationType.WARNING
                        )
                        fetchCurrentPlaybackState()
                    } else {
                        throw e
                    }
                }
            } catch (e: Exception) {
                handleSpotifyException("start playback", e)
            }
        }
    }

    fun pause() {
        if (!isPlaying) return
        CompletableFuture.runAsync {
            try {
                val pauseRequest = spotifyApi.pauseUsersPlayback().build()
                fetchCurrentPlaybackState()
                pauseRequest.execute()
                isPlaying = false
            } catch (e: Exception) {
                handleSpotifyException("pause playback", e)
            }
        }
    }

    fun resume() {
        if (isPlaying) return
        CompletableFuture.runAsync {
            try {
                val deviceId = getActiveDeviceId()
                if (deviceId == null) {
                    Shindo.getInstance().notificationManager.post(
                        TranslateText.SPOTIFY_PLAYBACK,
                        TranslateText.SPOTIFY_NO_ACTIVE_DEVICE,
                        NotificationType.ERROR
                    )
                    return@runAsync
                }
                val resumeRequest = spotifyApi.startResumeUsersPlayback()
                    .device_id(deviceId)
                    .build()
                fetchCurrentPlaybackState()
                resumeRequest.execute()
                isPlaying = true
            } catch (e: Exception) {
                if (e.message?.contains("Restriction violated") == true) {
                    ShindoLogger.warn("Resume playback restricted - likely due to Spotify Premium requirement or device limitations")
                    Shindo.getInstance().notificationManager.post(
                        TranslateText.SPOTIFY_PLAYBACK,
                        TranslateText.SPOTIFY_PREMIUM_REQUIRED,
                        NotificationType.WARNING
                    )
                    fetchCurrentPlaybackState()
                } else {
                    handleSpotifyException("resume playback", e)
                }
            }
        }
    }

    fun fetchAndUpdateVolume() {
        try {
            val playbackState = spotifyApi.informationAboutUsersCurrentPlayback.build().execute()
            if (playbackState?.device != null) {
                currentVolume = playbackState.device.volume_percent
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
        } catch (_: Exception) { }
        return currentVolume
    }

    fun setVolume(volumePercent: Int) {
        if (volumePercent == currentVolume) return
        CompletableFuture.runAsync {
            try {
                val playbackState = spotifyApi.informationAboutUsersCurrentPlayback.build().execute()
                if (playbackState != null) {
                    currentVolume = playbackState.device?.volume_percent ?: currentVolume
                    if (volumePercent == currentVolume) return@runAsync
                }
                spotifyApi.setVolumeForUsersPlayback(volumePercent).build().execute()
                currentVolume = volumePercent
            } catch (e: Exception) {
                handleSpotifyException("set volume", e)
            }
        }
    }

    fun nextTrack() {
        CompletableFuture.runAsync {
            try {
                spotifyApi.skipUsersPlaybackToNextTrack().build().execute()
                fetchCurrentPlaybackState()
                updatePlaybackState()
            } catch (e: Exception) {
                handleSpotifyException("skip to next track", e)
            }
        }
    }

    fun previousTrack() {
        CompletableFuture.runAsync {
            try {
                spotifyApi.skipUsersPlaybackToPreviousTrack().build().execute()
                fetchCurrentPlaybackState()
                updatePlaybackState()
            } catch (e: Exception) {
                handleSpotifyException("skip to previous track", e)
            }
        }
    }

    fun seekToPosition(positionMs: Long) {
        CompletableFuture.runAsync {
            try {
                spotifyApi.seekToPositionInCurrentlyPlayingTrack(positionMs.toInt()).build().execute()
                synchronized(this@MusicManager) {
                    trackPosition = positionMs
                    lastPositionUpdateTime = System.currentTimeMillis()
                    notifyTrackInfoUpdated()
                }
                scheduler.schedule({ synchronizePlaybackPosition() }, 300, TimeUnit.MILLISECONDS)
            } catch (e: Exception) {
                handleSpotifyException("seek to position", e)
            }
        }
    }

    private fun updatePlaybackState() {
        try {
            val currentlyPlaying = spotifyApi.usersCurrentlyPlayingTrack.build().execute()
            if (currentlyPlaying?.item != null) {
                currentTrack = currentlyPlaying.item as Track
                isPlaying = currentlyPlaying.is_playing
                trackPosition = currentlyPlaying.progress_ms.toLong()
                trackDuration = currentTrack!!.durationMs.toLong()
                notifyTrackInfoUpdated()
            }
        } catch (e: Exception) {
            ShindoLogger.error("Error updating playback state", e)
        }
    }

    private fun handleSpotifyException(action: String, e: Exception) {
        ShindoLogger.error("Failed to $action: ${e.message}", e)
        val errorText = when (action) {
            "start playback" -> TranslateText.SPOTIFY_PLAYBACK_START_FAILED
            "pause playback" -> TranslateText.SPOTIFY_PLAYBACK_PAUSE_FAILED
            "resume playback" -> TranslateText.SPOTIFY_PLAYBACK_RESUME_FAILED
            "set volume" -> TranslateText.SPOTIFY_VOLUME_SET_FAILED
            "play playlist" -> TranslateText.SPOTIFY_FAILED_TO_PLAY_PLAYLIST
            else -> TranslateText.ERROR
        }
        Shindo.getInstance().notificationManager.post(
            TranslateText.SPOTIFY_PLAYBACK,
            errorText,
            NotificationType.ERROR
        )
    }

    private fun getActiveDeviceId(): String? {
        return try {
            val devices = spotifyApi.usersAvailableDevices.build().execute()
                ?: return null
            if (devices.isEmpty()) {
                ShindoLogger.warn("No Spotify devices found")
                return null
            }
            for (device in devices) {
                if (device.is_active) return device.id
            }
            ShindoLogger.info("No active device found, using first available: ${devices[0].name}")
            devices[0].id
        } catch (e: Exception) {
            if (e is IOException || e is SpotifyWebApiException || e is ParseException) {
                ShindoLogger.error("Failed to get active device", e)
            }
            null
        }
    }

    fun getAlbumArtUrl(track: Track?): String? {
        val images = track?.album?.images ?: return null
        if (images.isEmpty()) return null
        val imageUrl = images[0].url ?: return null
        return try {
            albumArtCache.getCachedAlbumArtUrlAsync(track.id, imageUrl)
                .get(800, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            imageUrl
        } catch (_: Exception) {
            imageUrl
        }
    }

    private fun fetchCurrentPlaybackState() {
        try {
            val playbackState = spotifyApi.informationAboutUsersCurrentPlayback.build().execute()
                ?: return
            isPlaying = playbackState.is_playing
            trackPosition = playbackState.progress_ms.toLong()
            lastPositionUpdateTime = System.currentTimeMillis()
            playbackState.device?.let { currentVolume = it.volume_percent }
            val item = playbackState.item
            if (item != null && item is Track) {
                val newTrack = item
                if (currentTrack == null || currentTrack!!.id != newTrack.id) {
                    currentTrack = newTrack
                    trackDuration = newTrack.durationMs.toLong()
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
            if (rateLimiter.tryAcquire()) {
                if (!isAuthorized) {
                    ShindoLogger.warn("Spotify not authorized, skipping playback state update")
                    return@scheduleAtFixedRate
                }
                fetchCurrentPlaybackState()
            } else if (isPlaying) {
                val currentPosition = getCurrentPosition()
                if (currentPosition != trackPosition) {
                    trackPosition = currentPosition
                    notifyTrackInfoUpdated()
                }
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
                if (rateLimiter.tryAcquire()) {
                    val playbackState = spotifyApi.informationAboutUsersCurrentPlayback.build().execute()
                    if (playbackState != null) {
                        synchronized(this@MusicManager) {
                            isPlaying = playbackState.is_playing
                            trackPosition = playbackState.progress_ms.toLong()
                            lastPositionUpdateTime = System.currentTimeMillis()
                            val item = playbackState.item
                            if (item != null && item is Track) {
                                val newTrack = item
                                if (currentTrack == null || currentTrack!!.id != newTrack.id) {
                                    currentTrack = newTrack
                                    trackDuration = newTrack.durationMs.toLong()
                                }
                            }
                            notifyTrackInfoUpdated()
                        }
                    }
                }
            } catch (e: Exception) {
                ShindoLogger.error("Error during position sync: ${e.message}")
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
        try {
            val creds = spotifyApi.authorizationCodeRefresh().build().execute()
            spotifyApi.accessToken = creds.accessToken
            creds.refreshToken?.let { spotifyApi.refreshToken = it }
            saveTokens()
        } catch (e: Exception) {
            if (e is IOException || e is SpotifyWebApiException || e is ParseException) {
                ShindoLogger.error("Failed to refresh access token automatically", e)
                isAuthorized = false
                Shindo.getInstance().notificationManager.post(
                    TranslateText.SPOTIFY_AUTH,
                    TranslateText.SPOTIFY_AUTH_REFRESH_FAILED,
                    NotificationType.ERROR
                )
            }
        }
    }

    private fun scheduleTokenRefresh() {
        val refreshInterval = 3600L - 300
        tokenRefreshScheduler.scheduleAtFixedRate(
            this::refreshAccessToken,
            refreshInterval, refreshInterval, TimeUnit.SECONDS
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
            cacheKey
        ) { k: String? ->
            throttleRequest("playlists",
                Supplier<CompletableFuture<List<PlaylistSimplified>>> {
                    CompletableFuture.supplyAsync {
                        try {
                            val allPlaylists: MutableList<PlaylistSimplified> =
                                ArrayList()
                            var offset = 0
                            var hasMore = true
                            while (hasMore && offset < 200) {
                                val request =
                                    spotifyApi.listOfCurrentUsersPlaylists
                                        .limit(PLAYLIST_LIMIT)
                                        .offset(offset)
                                        .build()
                                val batch = request.execute().items
                                if (batch.isEmpty()) {
                                    hasMore = false
                                } else {
                                    allPlaylists.addAll(listOf(*batch))
                                    offset += batch.size
                                    Thread.sleep(THROTTLE_DELAY)
                                }
                            }
                            CompletableFuture.runAsync { prefetchPlaylistImages(allPlaylists) }
                            return@supplyAsync allPlaylists
                        } catch (e: Exception) {
                            error("Failed to fetch playlists", e)
                            return@supplyAsync emptyList<PlaylistSimplified>()
                        } finally {
                            playlistCache.remove(cacheKey)
                        }
                    }
                })
        }
    }

    private fun prefetchPlaylistImages(playlists: List<PlaylistSimplified>) {
        try {
            var i = 0
            while (i < playlists.size) {
                val end = minOf(i + BATCH_SIZE, playlists.size)
                val batch = playlists.subList(i, end)
                batch.filter { it.images != null && it.images.isNotEmpty() }
                    .forEach { getPlaylistImageUrl(it) }
                Thread.sleep(THROTTLE_DELAY)
                i = end
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun <T> throttleRequest(key: String, request: Supplier<CompletableFuture<T>>): CompletableFuture<T> {
        return CompletableFuture.supplyAsync {
            val lastTime = lastRequestTime.getOrDefault(key, 0L)
            val now = System.currentTimeMillis()
            val timeSinceLastRequest = now - lastTime
            if (timeSinceLastRequest < THROTTLE_DELAY) {
                try {
                    Thread.sleep(THROTTLE_DELAY - timeSinceLastRequest)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
            lastRequestTime[key] = System.currentTimeMillis()
            request.get().join()
        }
    }

    fun playPlaylist(playlistUri: String) {
        CompletableFuture.runAsync {
            try {
                val request = spotifyApi.startResumeUsersPlayback()
                    .context_uri(playlistUri)
                    .build()
                request.execute()
            } catch (e: Exception) {
                ShindoLogger.error("Failed to play playlist", e)
                handleSpotifyException("play playlist", e)
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
            ShindoLogger.warn("Using direct playlist image URL: ${e.message}")
            imageUrl
        }
    }

    fun getLyricsManager(): LyricsManager = lyricsManager
    fun getTrackPosition(): Long = trackPosition

    private class SimpleRateLimiter(requestsPerSecond: Double) {
        private val minTimeBetweenRequests = (1000.0 / requestsPerSecond).toLong()
        private var lastRequestTime: Long = 0

        @Synchronized
        fun tryAcquire(): Boolean {
            val now = System.currentTimeMillis()
            if (now - lastRequestTime >= minTimeBetweenRequests) {
                lastRequestTime = now
                return true
            }
            return false
        }
    }

    private inner class SpotifyCallbackHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            val query = exchange.requestURI.query
            val response = "Authorization successful! You can close this window now."
            exchange.sendResponseHeaders(200, response.length.toLong())
            val os: OutputStream = exchange.responseBody
            os.write(response.toByteArray())
            os.close()
            if (query != null && query.startsWith("code=")) {
                val code = query.substring(5)
                CompletableFuture.runAsync { requestAccessToken(code) }
            } else {
                ShindoLogger.warn("Received callback without authorization code")
            }
        }
    }

    companion object {
        private val REDIRECT_URI = SpotifyHttpManager.makeUri("http://127.0.0.1:8888/callback")
        private const val TOKEN_FILE_NAME = "spotify_tokens.properties"
        private const val CREDENTIALS_FILE_NAME = "spotify_credentials.properties"
        private const val SEARCH_LIMIT = 30
        private const val PLAYLIST_LIMIT = 50
        private const val PLAYBACK_UPDATE_INTERVAL = 1000L
        private const val BATCH_SIZE = 20
        private const val THROTTLE_DELAY = 50L
    }
}
