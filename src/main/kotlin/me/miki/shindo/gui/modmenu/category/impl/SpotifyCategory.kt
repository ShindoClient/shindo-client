package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.libs.spotify.model_objects.specification.PlaylistSimplified
import me.miki.shindo.libs.spotify.model_objects.specification.Track
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.music.LyricsManager
import me.miki.shindo.management.music.MusicManager
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.notification.NotificationType
import me.miki.shindo.ui.comp.inputs.CompSlider
import me.miki.shindo.ui.comp.inputs.CompTextBox
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiConfirmOpenLink
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.File
import java.lang.ref.WeakReference
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class SpotifyCategory(parent: GuiModMenu) : Category(parent, TranslateText.SPOTIFY, LegacyIcon.SPOTIFY, true, true), MusicManager.TrackInfoCallback {

    private val volumeSlider = CompSlider(
        InternalSettingsMod.instance.getVolumeSetting()
            ?: throw IllegalStateException("Internal volume setting is not registered")
    )
    private val textBox = CompTextBox()
    private val clientIdTextBox = CompTextBox()
    private val clientSecretTextBox = CompTextBox()
    private val parentRef = WeakReference(parent)
    private val searchDebouncer: ScheduledExecutorService
    private val isSearching = AtomicBoolean(false)
    private val noColour = Color(0, 0, 0, 0)
    private val lyricsScroll = Scroll()
    private var pendingSearch: ScheduledFuture<*>? = null

    @Volatile
    private var searchResults: List<Track>? = null

    @Volatile
    private var searchPlaylistResults: List<PlaylistSimplified>? = null

    @Volatile
    private var userPlaylists: List<PlaylistSimplified>? = null

    private var openDownloader = false
    private var showSetupScreen = false
    private var setupError = false
    private var trackPosition = 0L
    private var trackDuration = 0L
    private var lastVolumeChangeTime = 0L
    private var lastSearchQuery = ""
    private var currentTrackId: String? = null
    private var showConnectButton = true
    private var showingLyrics = false
    private var currentHighlightedLyricIndex = -1

    init {
        searchDebouncer = Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "Search-Debouncer").apply { isDaemon = true }
        }
        initializeComponents()
    }

    private fun initializeComponents() {
        textBox.setDefaultText("Enter a Spotify link")
        clientIdTextBox.setDefaultText(TranslateText.SPOTIFY_CLIENT_ID.text)
        clientSecretTextBox.setDefaultText(TranslateText.SPOTIFY_CLIENT_SECRET.text)
        volumeSlider.setCircle(false)
        volumeSlider.setShowValue(false)
    }

    override fun initGui() {
    }

    override fun initCategory() {
        scroll.resetAll()
        lyricsScroll.resetAll()
        showingLyrics = false

        val musicManager = Shindo.getInstance().musicManager

        showSetupScreen = !musicManager.hasCredentials()
        showConnectButton = !showSetupScreen && !musicManager.isAuthorized

        musicManager.setTrackInfoCallback(this)

        if (!showSetupScreen) {
            fetchUserPlaylists()

            CompletableFuture.runAsync {
                try {
                    musicManager.fetchAndUpdateVolume()
                    val actualVolume = musicManager.volume
                    volumeSlider.getSetting().setValue(actualVolume / 100.0)
                } catch (e: Exception) {
                    ShindoLogger.warn("Failed to sync volume slider: " + e.message)
                }
            }
        } else {
            setupError = false
            clientIdTextBox.setText("")
            clientSecretTextBox.setText("")
        }
    }

    private fun fetchUserPlaylists() {
        Shindo.getInstance().musicManager.userPlaylists
            .thenAccept { playlists ->
                if (playlists != null) {
                    java.util.Collections.reverse(playlists)
                }
                userPlaylists = playlists
            }
            .exceptionally { ex ->
                ShindoLogger.error("Failed to fetch user playlists: " + ex.message)
                null
            }
    }

    private fun openConfirmDialog(uri: String) {
        val mc = Minecraft.getMinecraft()
        val gui = GuiConfirmOpenLink({ result, _ ->
            if (result) {
                tryOpenBrowser(uri)
            }
            mc.displayGuiScreen(parentRef.get())
        }, uri, 0, true)
        gui.disableSecurityWarning()
        mc.displayGuiScreen(gui)
    }

    private fun tryOpenBrowser(uri: String) {
        try {
            val desktopClass = Class.forName("java.awt.Desktop")
            val desktop = desktopClass.getMethod("getDesktop").invoke(null)
            desktopClass.getMethod("browse", URI::class.java).invoke(desktop, URI(uri))
        } catch (e: Exception) {
            Shindo.getInstance().notificationManager.post(
                TranslateText.SPOTIFY_AUTH,
                TranslateText.SPOTIFY_FAIL_BROWSER,
                NotificationType.ERROR
            )
            ShindoLogger.error(e.toString())
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager!!
        val colorManager = instance.colorManager
        val palette = colorManager.palette
        val accentColor = colorManager.currentColor
        val musicManager = instance.musicManager

        if (!isSearching.get()) {
            checkAndUpdateSearch()
        }

        if (showingLyrics) {
            lyricsScroll.onScroll()
            lyricsScroll.onAnimation()
        }

        if (showSetupScreen) {
            drawSetupScreen(nvg, mouseX, mouseY, palette, accentColor)
        } else if (showConnectButton) {
            drawConnectButton(nvg, mouseX, mouseY)
        } else {
            nvg.save()
            try {
                if (showingLyrics) {
                    drawLyricsView(nvg, palette, accentColor, musicManager, mouseX, mouseY)
                } else {
                    nvg.save()
                    nvg.translate(0f, scroll.getValue())
                    drawSearchResults(nvg, palette)
                    drawUserPlaylists(nvg, palette)
                    nvg.restore()

                    drawControlBar(nvg, palette, musicManager)
                    drawPlaybackControls(nvg, palette, musicManager)
                    drawVolumeSlider(nvg, palette, mouseX, mouseY, partialTicks)
                    drawLyricsButton(nvg, palette, mouseX, mouseY)
                    drawProgressBar(nvg, accentColor, palette)
                }
            } finally {
                nvg.restore()
            }

            if (!showingLyrics) {
                nvg.drawVerticalGradientRect(getX() + 15f, getY().toFloat(), getWidth() - 30f, 12f,
                    palette.getBackgroundColor(ColorType.NORMAL), noColour)
                nvg.drawVerticalGradientRect(getX() + 15f, getY() + getHeight() - 58f, getWidth() - 30f, 12f,
                    noColour, palette.getBackgroundColor(ColorType.NORMAL))
            }
        }

        updateScroll()
    }

    private fun drawSetupScreen(nvg: NanoVGManager, mouseX: Int, mouseY: Int, palette: ColorPalette, accentColor: AccentColor) {
        val centerX = getX() + (getWidth() / 2f)
        val centerY = getY() + (getHeight() / 2f) - 40f

        nvg.drawCenteredText(TranslateText.SPOTIFY_SETUP.text,
            centerX, getY() + 40f,
            palette.getFontColor(ColorType.DARK), 20f, Fonts.MEDIUM)

        nvg.drawText(TranslateText.SPOTIFY_CLIENT_ID.text,
            centerX - 150f, centerY - 10f,
            palette.getFontColor(ColorType.NORMAL), 12f, Fonts.MEDIUM)

        clientIdTextBox.setWidth(300f)
        clientIdTextBox.setHeight(20f)
        clientIdTextBox.setX(centerX - 150f)
        clientIdTextBox.setY(centerY + 5f)
        clientIdTextBox.draw(mouseX, mouseY, 0f)

        nvg.drawText(TranslateText.SPOTIFY_CLIENT_SECRET.text,
            centerX - 150f, centerY + 40f,
            palette.getFontColor(ColorType.NORMAL), 12f, Fonts.MEDIUM)

        clientSecretTextBox.setWidth(300f)
        clientSecretTextBox.setHeight(20f)
        clientSecretTextBox.setX(centerX - 150f)
        clientSecretTextBox.setY(centerY + 55f)
        clientSecretTextBox.draw(mouseX, mouseY, 0f)

        val tutorialButtonY = centerY + 100f
        val tutorialHovered = MouseUtils.isInside(mouseX, mouseY,
            centerX - 150f, tutorialButtonY, 140f, 30f)

        nvg.drawRoundedRect(centerX - 150f, tutorialButtonY, 140f, 30f, 5f,
            palette.getBackgroundColor(ColorType.DARK))

        nvg.drawCenteredText(TranslateText.SPOTIFY_TUTORIAL.text,
            centerX - 80f, tutorialButtonY + 11f,
            if (tutorialHovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
            11f, Fonts.MEDIUM)

        val setupButtonX = centerX + 10f
        val setupHovered = MouseUtils.isInside(mouseX, mouseY,
            setupButtonX, tutorialButtonY, 140f, 30f)

        nvg.drawRoundedRect(setupButtonX, tutorialButtonY, 140f, 30f, 5f,
            if (setupHovered) accentColor.interpolateColor else palette.getBackgroundColor(ColorType.DARK))

        nvg.drawCenteredText(TranslateText.SPOTIFY_NEXT.text,
            setupButtonX + 70f, tutorialButtonY + 11f,
            if (setupHovered) Color.WHITE else palette.getFontColor(ColorType.NORMAL),
            11f, Fonts.MEDIUM)

        if (setupError) {
            Shindo.getInstance().notificationManager.post(TranslateText.MUSIC, TranslateText.SPOTIFY_INVALID_CREDENTIALS, NotificationType.ERROR)
            setupError = false
        }
    }
    private fun drawSearchResults(nvg: NanoVGManager, palette: ColorPalette) {
        if (searchResults == null && searchPlaylistResults == null) {
            return
        }

        var offsetY = 13f
        val localResults = searchResults
        if (localResults != null) {
            for (track in localResults) {
                if (offsetY + 46f >= -scroll.getValue() && offsetY <= -scroll.getValue() + getHeight()) {
                    drawTrackEntry(nvg, palette, track, offsetY)
                }
                offsetY += 56f
            }
        }
        val playlistResults = searchPlaylistResults
        if (playlistResults != null) {
            for (playlist in playlistResults) {
                if (playlist == null) continue
                if (offsetY + 46f >= -scroll.getValue() && offsetY <= -scroll.getValue() + getHeight()) {
                    drawPlaylistEntry(nvg, palette, playlist, offsetY)
                }
                offsetY += 56f
            }
        }
    }

    private fun drawTrackEntry(nvg: NanoVGManager, palette: ColorPalette, track: Track, offsetY: Float) {
        nvg.drawRoundedRect(getX() + 15f, getY() + offsetY, getWidth() - 30f, 46f, 8f, palette.getBackgroundColor(ColorType.DARK))

        drawTrackImage(nvg, track, offsetY)
        drawTrackInfo(nvg, palette, track, offsetY)

        if (DEBUG_HITBOXES) {
            nvg.drawRect(getX() + 15f, getY() + offsetY, getWidth() - 30f, 46f, DEBUG_COLOR)

            nvg.drawRect(getX() + 20f, getY() + offsetY + 5f, 36f, 36f, DEBUG_COLOR)
            nvg.drawRect(getX() + getWidth() - 60f, getY() + offsetY + 15f, 16f, 16f, DEBUG_COLOR)
        }
    }

    private fun drawTrackImage(nvg: NanoVGManager, track: Track?, offsetY: Float) {
        if (track == null) {
            drawPlaceholderImage(nvg, offsetY)
            return
        }

        val albumArtUrl = Shindo.getInstance().musicManager.getAlbumArtUrl(track)
        if (albumArtUrl != null && File(albumArtUrl).exists()) {
            nvg.drawRoundedImage(File(albumArtUrl), getX() + 20f, getY() + offsetY + 5f, 36f, 36f, 6f)
        } else {
            drawPlaceholderImage(nvg, offsetY)
        }
    }

    private fun drawTrackInfo(nvg: NanoVGManager, palette: ColorPalette, track: Track, offsetY: Float) {
        val trackName = nvg.getLimitText(track.name, 11f, Fonts.MEDIUM, 280f)
        nvg.drawText(trackName, getX() + 63f, getY() + offsetY + 9f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
        nvg.drawText(track.artists[0].name, getX() + 63f, getY() + offsetY + 25f, palette.getFontColor(ColorType.NORMAL), 9f, Fonts.MEDIUM)
        nvg.drawText(LegacyIcon.PLUS_SQUARE, getX() + getWidth() - 60f, getY() + offsetY + 15f, palette.getFontColor(ColorType.NORMAL), 16f, Fonts.LEGACYICON)
    }

    private fun drawPlaceholderImage(nvg: NanoVGManager, offsetY: Float) {
        try {
            nvg.drawRoundedImage(PLACEHOLDER_IMAGE, getX() + 20f, getY() + offsetY + 5f, 36f, 36f, 6f)
        } catch (e: Exception) {
            nvg.drawRoundedRect(getX() + 20f, getY() + offsetY + 5f, 36f, 36f, 6f, Color(50, 50, 50))
        }
    }

    private fun drawPlaylistEntry(nvg: NanoVGManager, palette: ColorPalette, playlist: PlaylistSimplified, offsetY: Float) {
        nvg.drawRoundedRect(getX() + 15f, getY() + offsetY, getWidth() - 30f, 46f, 8f, palette.getBackgroundColor(ColorType.DARK))

        val imageUrl = Shindo.getInstance().musicManager.getPlaylistImageUrl(playlist)
        if (imageUrl != null) {
            val imageFile = File(imageUrl)
            if (imageFile.exists()) {
                nvg.drawRoundedImage(imageFile, getX() + 20f, getY() + offsetY + 5f, 36f, 36f, 6f)
            } else {
                drawPlaceholderImage(nvg, offsetY)
            }
        } else {
            drawPlaceholderImage(nvg, offsetY)
        }

        val playlistName = playlist.name ?: "Untitled Playlist"
        nvg.drawText(nvg.getLimitText(playlistName, 11f, Fonts.MEDIUM, 280f), getX() + 63f, getY() + offsetY + 9f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)

        var ownerName = "Unknown Artist"
        if (playlist.owner != null && playlist.owner.displayName != null) {
            ownerName = playlist.owner.displayName
        }

        nvg.drawText(ownerName, getX() + 63f, getY() + offsetY + 25f, palette.getFontColor(ColorType.NORMAL), 9f, Fonts.MEDIUM)

        nvg.drawText(LegacyIcon.PLAY, getX() + getWidth() - 60f, getY() + offsetY + 15f, palette.getFontColor(ColorType.NORMAL), 16f, Fonts.LEGACYICON)

        if (DEBUG_HITBOXES) {
            nvg.drawRect(getX() + 15f, getY() + offsetY, getWidth() - 30f, 46f, DEBUG_COLOR)
            nvg.drawRect(getX() + 20f, getY() + offsetY + 5f, 36f, 36f, DEBUG_COLOR)
        }
    }

    private fun drawUserPlaylists(nvg: NanoVGManager, palette: ColorPalette) {
        val playlists = userPlaylists ?: return

        var offsetY = 13f + (searchResults?.size ?: 0) * 56f + (searchPlaylistResults?.size ?: 0) * 56f
        for (playlist in playlists) {
            if (playlist == null) {
                continue
            }

            nvg.drawRoundedRect(getX() + 15f, getY() + offsetY, getWidth() - 30f, 46f, 8f, palette.getBackgroundColor(ColorType.DARK))

            val imageUrl = Shindo.getInstance().musicManager.getPlaylistImageUrl(playlist)
            if (imageUrl != null) {
                val imageFile = File(imageUrl)
                if (imageFile.exists()) {
                    nvg.drawRoundedImage(imageFile, getX() + 20f, getY() + offsetY + 5f, 36f, 36f, 6f)
                } else {
                    drawPlaceholderImage(nvg, offsetY)
                }
            } else {
                drawPlaceholderImage(nvg, offsetY)
            }

            val playlistName = playlist.name ?: "Untitled Playlist"
            nvg.drawText(nvg.getLimitText(playlistName, 11f, Fonts.MEDIUM, 280f), getX() + 63f, getY() + offsetY + 9f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)

            var ownerName = "Unknown Artist"
            if (playlist.owner != null && playlist.owner.displayName != null) {
                ownerName = playlist.owner.displayName
            }

            nvg.drawText(ownerName, getX() + 63f, getY() + offsetY + 25f, palette.getFontColor(ColorType.NORMAL), 9f, Fonts.MEDIUM)

            if (DEBUG_HITBOXES) {
                nvg.drawRect(getX() + 15f, getY() + offsetY, getWidth() - 30f, 46f, DEBUG_COLOR)
                nvg.drawRect(getX() + 20f, getY() + offsetY + 5f, 36f, 36f, DEBUG_COLOR)
            }

            offsetY += 56f
        }
    }
    private fun drawControlBar(nvg: NanoVGManager, palette: ColorPalette, musicManager: MusicManager) {
        nvg.drawRoundedRectVarying(getX().toFloat(), getY() + getHeight() - 46f, getWidth().toFloat(), 46f, 0f, 0f, 0f, 12f, palette.getBackgroundColor(ColorType.DARK))

        val currentTrack = musicManager.currentTrack
        if (currentTrack != null) {
            val albumArtUrl = musicManager.getAlbumArtUrl(currentTrack)
            if (albumArtUrl != null) {
                val albumArtFile = File(albumArtUrl)
                if (albumArtFile.exists()) {
                    nvg.drawRoundedImage(albumArtFile, getX() + 4f, getY() + getHeight() - 43f, 36f, 36f, 6f)
                } else {
                    nvg.drawRoundedRect(getX() + 4f, getY() + getHeight() - 43f, 36f, 36f, 6f, Color(50, 50, 50))
                    try {
                        nvg.drawRoundedImage(PLACEHOLDER_IMAGE, getX() + 4f, getY() + getHeight() - 43f, 36f, 36f, 6f)
                    } catch (ignored: Exception) {
                    }
                }
            } else {
                nvg.drawRoundedRect(getX() + 4f, getY() + getHeight() - 43f, 36f, 36f, 6f, Color(50, 50, 50))
                try {
                    nvg.drawRoundedImage(PLACEHOLDER_IMAGE, getX() + 4f, getY() + getHeight() - 43f, 36f, 36f, 6f)
                } catch (ignored: Exception) {
                }
            }

            nvg.drawText(nvg.getLimitText(currentTrack.name, 9f, Fonts.MEDIUM, 100f), getX() + 45f, getY() + getHeight() - 39f, palette.getFontColor(ColorType.DARK), 9f, Fonts.MEDIUM)
            nvg.drawText(nvg.getLimitText(currentTrack.artists[0].name, 9f, Fonts.MEDIUM, 100f), getX() + 45f, getY() + getHeight() - 27f, palette.getFontColor(ColorType.NORMAL), 9f, Fonts.MEDIUM)
        } else {
            nvg.drawRoundedRect(getX() + 4f, getY() + getHeight() - 43f, 36f, 36f, 6f, Color(50, 50, 50))
            try {
                nvg.drawRoundedImage(PLACEHOLDER_IMAGE, getX() + 4f, getY() + getHeight() - 43f, 36f, 36f, 6f)
            } catch (ignored: Exception) {
            }
            nvg.drawText(TranslateText.NOTHING_IS_PLAYING.text, getX() + 45f, getY() + getHeight() - 33f, palette.getFontColor(ColorType.DARK), 9f, Fonts.MEDIUM)
        }
    }

    private fun drawPlaybackControls(nvg: NanoVGManager, palette: ColorPalette, musicManager: MusicManager) {
        val centerX = getX() + (getWidth() / 2f)
        val centerY = getY() + getHeight() - 32f
        val normalColor = palette.getFontColor(ColorType.NORMAL)

        nvg.drawText(LegacyIcon.BACK, centerX - 32f, centerY, normalColor, 16f, Fonts.LEGACYICON)
        nvg.drawText(if (musicManager.isPlaying) LegacyIcon.PAUSE else LegacyIcon.PLAY, centerX - 8f, centerY, normalColor, 16f, Fonts.LEGACYICON)
        nvg.drawText(LegacyIcon.FORWARD, centerX + 16f, centerY, normalColor, 16f, Fonts.LEGACYICON)
        if (DEBUG_HITBOXES) {
            nvg.drawRect(centerX - 24f - 8f, centerY, 16f, 16f, DEBUG_COLOR)
            nvg.drawRect(centerX - 8f, centerY, 16f, 16f, DEBUG_COLOR)
            nvg.drawRect(centerX + 24f - 8f, centerY, 16f, 16f, DEBUG_COLOR)
        }
    }

    private fun drawVolumeSlider(nvg: NanoVGManager, palette: ColorPalette, mouseX: Int, mouseY: Int, partialTicks: Float) {
        volumeSlider.setX(getX() + getWidth() - 72f)
        volumeSlider.setY(getY() + getHeight() - 20f)
        volumeSlider.setWidth(62f)
        volumeSlider.setHeight(4.5f)
        volumeSlider.draw(mouseX, mouseY, partialTicks)

        val volume = (volumeSlider.getSetting().getValueFloat() * 100).toInt()
        val volumeIcon = getVolumeIcon(volume)
        nvg.drawText(volumeIcon, getX() + getWidth() - 94f, getY() + getHeight() - 26f, palette.getFontColor(ColorType.NORMAL), 16f, Fonts.LEGACYICON)
    }

    private fun getVolumeIcon(volume: Int): String {
        if (volume == 0) {
            return LegacyIcon.VOLUME_X
        }
        if (volume > 80) {
            return LegacyIcon.VOLUME_2
        }
        if (volume > 40) {
            return LegacyIcon.VOLUME_1
        }
        return LegacyIcon.VOLUME
    }

    private fun drawProgressBar(nvg: NanoVGManager, accentColor: AccentColor, palette: ColorPalette) {
        if (trackDuration <= 0) {
            return
        }

        val progressBarWidth = getWidth() - 40
        val progressBarY = getY() + getHeight() - 5
        nvg.drawRoundedRect(getX() + 20f, progressBarY.toFloat(), progressBarWidth.toFloat(), 2f, 1f, palette.getBackgroundColor(ColorType.NORMAL))
        val progress = trackPosition.toFloat() / trackDuration.toFloat()
        nvg.drawRoundedRect(getX() + 20f, progressBarY.toFloat(), progressBarWidth * progress, 2f, 1f, accentColor.interpolateColor)
    }

    private fun checkAndUpdateSearch() {
        val parent = parentRef.get() ?: return

        val currentSearchQuery = parent.getSearchBox().getText()
        if (currentSearchQuery != lastSearchQuery) {
            scheduleSearch(currentSearchQuery)
            lastSearchQuery = currentSearchQuery
        }
    }

    private fun scheduleSearch(query: String) {
        if (query.isEmpty()) {
            searchResults = null
            searchPlaylistResults = null
            return
        }

        val currentPending = pendingSearch
        if (currentPending != null && !currentPending.isDone) {
            currentPending.cancel(false)
        }

        pendingSearch = searchDebouncer.schedule({
            if (isSearching.compareAndSet(false, true)) {
                try {
                    val musicManager = Shindo.getInstance().musicManager
                    val tracksFuture = musicManager.searchTracks(query)
                    val playlistsFuture = musicManager.searchPlaylists(query)

                    val results = tracksFuture.join()
                    val playlists = playlistsFuture.join()

                    searchResults = results
                    searchPlaylistResults = playlists

                    if (results != null) {
                        val visibleTracks = kotlin.math.min(results.size, 5)
                        for (i in 0 until visibleTracks) {
                            musicManager.getAlbumArtUrl(results[i])
                        }
                    }
                    if (playlists != null) {
                        val visiblePlaylists = kotlin.math.min(playlists.size, 5)
                        for (i in 0 until visiblePlaylists) {
                            musicManager.getPlaylistImageUrl(playlists[i])
                        }
                    }
                } catch (ex: Exception) {
                    ShindoLogger.error("Search failed", ex)
                    Shindo.getInstance().notificationManager.post(TranslateText.MUSIC, TranslateText.SPOTIFY_SEARCH_FAILED, NotificationType.ERROR)
                } finally {
                    isSearching.set(false)
                }
            }
        }, SEARCH_DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)
    }
    private fun drawLyricsButton(nvg: NanoVGManager, palette: ColorPalette, mouseX: Int, mouseY: Int) {
        val buttonX = getX() + getWidth() - 116f
        val buttonY = getY() + getHeight() - 26f

        val isHovered = MouseUtils.isInside(mouseX, mouseY, buttonX, buttonY, 16f, 16f)

        nvg.drawText(LegacyIcon.LIST, buttonX, buttonY, if (isHovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL), 16f, Fonts.LEGACYICON)

        if (DEBUG_HITBOXES) {
            nvg.drawRect(buttonX, buttonY, 16f, 16f, DEBUG_COLOR)
        }
    }

    private fun drawLyricsView(nvg: NanoVGManager, palette: ColorPalette, accentColor: AccentColor, musicManager: MusicManager, mouseX: Int, mouseY: Int) {
        nvg.drawRoundedRect(getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight() - 46f, 0f, palette.getBackgroundColor(ColorType.NORMAL))

        val currentTrack = musicManager.currentTrack

        val backButtonX = getX() + 15f
        val backButtonY = getY() + 15f

        val isBackHovered = MouseUtils.isInside(mouseX, mouseY, backButtonX, backButtonY, 16f, 16f)

        nvg.drawText(LegacyIcon.ARROW_LEFT, backButtonX, backButtonY, if (isBackHovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL), 16f, Fonts.LEGACYICON)

        if (currentTrack != null) {
            val lyricsManager = musicManager.lyricsManager
            if (lyricsManager != null) {
                if (MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight() - 46f)) {
                    lyricsScroll.onScroll()
                }
                lyricsScroll.onAnimation()

                drawScrollableLyrics(nvg, palette, accentColor, musicManager, mouseX, mouseY, 0f, trackPosition)
            } else {
                nvg.drawCenteredText("Lyrics feature not available", getX() + (getWidth() / 2f), getY() + getHeight() / 2.7f, palette.getFontColor(ColorType.NORMAL), 14f, Fonts.MEDIUM)
            }
        } else {
            nvg.drawCenteredText("No track is currently playing", getX() + (getWidth() / 2f), getY() + getHeight() / 2.7f, palette.getFontColor(ColorType.NORMAL), 14f, Fonts.MEDIUM)
        }

        drawControlBar(nvg, palette, musicManager)
        drawPlaybackControls(nvg, palette, musicManager)
        drawVolumeSlider(nvg, palette, mouseX, mouseY, 0f)
        drawProgressBar(nvg, accentColor, palette)
    }

    private fun drawScrollableLyrics(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        musicManager: MusicManager,
        mouseX: Int,
        mouseY: Int,
        startY: Float,
        currentPosition: Long
    ) {
        val lyricsManager = musicManager.lyricsManager ?: return
        val lyrics = lyricsManager.currentLyrics

        if (lyrics == null || lyrics.isError || lyrics.lines.isEmpty()) {
            nvg.drawCenteredText("No lyrics available for this track", getX() + (getWidth() / 2f), getY() + getHeight() / 2.7f, palette.getFontColor(ColorType.NORMAL), 14f, Fonts.MEDIUM)
            return
        }

        val allLines = lyrics.lines

        lyricsManager.updateCurrentLineIndex(currentPosition)
        val currentLineIndex = lyricsManager.currentLineIndex

        val lyricsAreaHeight = getHeight() - startY - 46f

        nvg.save()
        nvg.scissor(getX() + 15f, getY() + startY, getWidth() - 30f, lyricsAreaHeight)

        val baseLineHeight = 30f
        var totalContentHeight = 0f
        val yOffset = lyricsScroll.getValue()

        currentHighlightedLyricIndex = -1

        val lineHeights = IntArray(allLines.size)
        val wrappedLines = arrayOfNulls<Array<String>>(allLines.size)

        val maxTextWidth = getWidth() - 60f

        for (i in allLines.indices) {
            val line = allLines[i]
            if (line == null) {
                lineHeights[i] = baseLineHeight.toInt()
                wrappedLines[i] = emptyArray()
                continue
            }

            val lyricsText = extractLyricsText(line)
            if (lyricsText.isEmpty()) {
                lineHeights[i] = baseLineHeight.toInt()
                wrappedLines[i] = emptyArray()
                continue
            }

            val fontSize = if (i == currentLineIndex) 14f else 12f

            val wrapped = wrapText(nvg, lyricsText, fontSize, Fonts.MEDIUM, maxTextWidth)
            wrappedLines[i] = wrapped

            val linesCount = wrapped.size
            lineHeights[i] = if (linesCount <= 1) baseLineHeight.toInt() else (fontSize * linesCount * 1.0f + 10).toInt()

            totalContentHeight += lineHeights[i].toFloat()
        }

        var currentY = getY() + startY + yOffset
        val visibleTop = getY() + startY
        val visibleBottom = visibleTop + lyricsAreaHeight

        for (i in allLines.indices) {
            val line = allLines[i]
            if (line == null) {
                currentY += lineHeights[i]
                continue
            }

            if (currentY + lineHeights[i] < visibleTop || currentY > visibleBottom) {
                currentY += lineHeights[i]
                continue
            }

            val isCurrentLine = i == currentLineIndex
            val isHovered = MouseUtils.isInside(mouseX, mouseY, getX() + 20f, currentY, getWidth() - 40f, lineHeights[i].toFloat())

            if (isHovered) {
                currentHighlightedLyricIndex = i
            }

            val lineColor: Color
            val fontSize: Float

            if (isCurrentLine) {
                lineColor = accentColor.interpolateColor
                fontSize = 14f

                nvg.drawRoundedRect(getX() + 20f, currentY, getWidth() - 40f, lineHeights[i].toFloat(), 4f,
                    Color(accentColor.color1.red, accentColor.color1.green, accentColor.color1.blue, 30))
            } else if (isHovered) {
                lineColor = palette.getFontColor(ColorType.DARK)
                fontSize = 12f
            } else {
                lineColor = palette.getFontColor(ColorType.NORMAL)
                fontSize = 12f
            }

            val wrapped = wrappedLines[i] ?: emptyArray()
            if (wrapped.isNotEmpty()) {
                val lineSpacing = 1.0f
                var wrapOffset = 0f

                for (wrappedLine in wrapped) {
                    val textWidth = nvg.getTextWidth(wrappedLine, fontSize, Fonts.MEDIUM)
                    val textX = getX() + (getWidth() / 2f) - (textWidth / 2f)
                    val textY = currentY + wrapOffset + (fontSize / 2f)

                    if (isCurrentLine) {
                        nvg.drawTextGlowing(wrappedLine, textX, textY, lineColor, 8f, fontSize, Fonts.MEDIUM)
                    } else {
                        nvg.drawText(wrappedLine, textX, textY, lineColor, fontSize, Fonts.MEDIUM)
                    }

                    wrapOffset += fontSize * lineSpacing
                }
            }

            currentY += lineHeights[i]
        }

        nvg.restore()

        val maxScroll = kotlin.math.max(0f, totalContentHeight - lyricsAreaHeight + 20f)
        lyricsScroll.maxScroll = maxScroll

        if (lyricsScroll.getValue() < 0) {
            nvg.drawVerticalGradientRect(getX() + 15f, getY() + startY, getWidth() - 30f, 12f, palette.getBackgroundColor(ColorType.NORMAL), noColour)
        }

        if (-lyricsScroll.getValue() < maxScroll) {
            nvg.drawVerticalGradientRect(getX() + 15f, getY() + startY + lyricsAreaHeight - 12f, getWidth() - 30f, 12f, noColour, palette.getBackgroundColor(ColorType.NORMAL))
        }
    }

    private fun wrapText(nvg: NanoVGManager, text: String?, fontSize: Float, font: me.miki.shindo.management.nanovg.font.Font, maxWidth: Float): Array<String> {
        if (text == null || text.isEmpty()) {
            return emptyArray()
        }

        if (nvg.getTextWidth(text, fontSize, font) <= maxWidth) {
            return arrayOf(text)
        }

        val lines = ArrayList<String>()
        val words = text.split(" ")
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = currentLine.toString() + (if (currentLine.isNotEmpty()) " " else "") + word
            if (nvg.getTextWidth(testLine, fontSize, font) <= maxWidth) {
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }
                currentLine.append(word)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder()
                }

                if (nvg.getTextWidth(word, fontSize, font) > maxWidth) {
                    var partialWord = StringBuilder()
                    for (c in word.toCharArray()) {
                        val testWord = partialWord.toString() + c
                        if (nvg.getTextWidth(testWord, fontSize, font) <= maxWidth) {
                            partialWord.append(c)
                        } else {
                            lines.add(partialWord.toString())
                            partialWord = StringBuilder().append(c)
                        }
                    }

                    if (partialWord.isNotEmpty()) {
                        currentLine = partialWord
                    }
                } else {
                    currentLine.append(word)
                }
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines.toTypedArray()
    }

    private fun extractLyricsText(line: LyricsManager.LyricsLine): String {
        if (line.words != null && line.words.isNotEmpty()) {
            return line.words
        }

        if (line.romanizedWords != null && line.romanizedWords.isNotEmpty()) {
            return line.romanizedWords
        }

        return ""
    }
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight().toFloat())) {
            return
        }

        if (showSetupScreen) {
            handleSetupScreenClick(mouseX, mouseY, mouseButton)
            return
        }

        if (openDownloader) {
            handleDownloaderClick(mouseX, mouseY, mouseButton)
            return
        }

        if (showConnectButton) {
            if (MouseUtils.isInside(mouseX, mouseY, getX() + (getWidth() / 2f) - 75f, getY() + (getHeight() / 2f) - 20f, 150f, 40f) && mouseButton == 0) {
                openConfirmDialog(Shindo.getInstance().musicManager.authorizationCodeUri)
                showConnectButton = false
            }
            return
        }

        if (!showingLyrics && mouseButton == 0) {
            val lyricsButtonX = getX() + getWidth() - 116f
            val lyricsButtonY = getY() + getHeight() - 26f

            if (MouseUtils.isInside(mouseX, mouseY, lyricsButtonX, lyricsButtonY, 16f, 16f)) {
                showingLyrics = true
                lyricsScroll.resetAll()

                val musicManager = Shindo.getInstance().musicManager
                if (musicManager.currentTrack != null && musicManager.lyricsManager != null) {
                    musicManager.lyricsManager.fetchLyrics(musicManager.currentTrack)
                }
                return
            }
        }

        val isInControlBar = mouseY >= getY() + getHeight() - 46
        if (mouseButton == 0 && isInControlBar) {
            handleControlBarClick(mouseX, mouseY)
            return
        }

        if (showingLyrics) {
            if (MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + 15f, 16f, 16f) && mouseButton == 0) {
                showingLyrics = false
                return
            }

            if (currentHighlightedLyricIndex >= 0 && mouseButton == 0) {
                val musicManager = Shindo.getInstance().musicManager
                val lyricsManager = musicManager.lyricsManager
                val lyrics = lyricsManager.currentLyrics

                if (lyrics != null && !lyrics.isError && currentHighlightedLyricIndex < lyrics.lines.size) {
                    val line = lyrics.lines[currentHighlightedLyricIndex]
                    if (line != null) {
                        val startTime = line.startTime
                        musicManager.seekToPosition(startTime)
                    }
                }
                return
            }
            return
        }

        if (mouseButton == 0 && searchResults != null && !showingLyrics) {
            handleTrackClick(mouseX, mouseY)
        } else if (mouseButton == 0 && userPlaylists != null && !showingLyrics) {
            handlePlaylistClick(mouseX, mouseY)
        }
    }

    private fun handleSetupScreenClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return

        clientIdTextBox.mouseClicked(mouseX, mouseY, mouseButton)
        clientSecretTextBox.mouseClicked(mouseX, mouseY, mouseButton)

        val centerX = getX() + (getWidth() / 2f)
        val centerY = getY() + (getHeight() / 2f) - 40f
        val tutorialButtonY = centerY + 100f

        if (MouseUtils.isInside(mouseX, mouseY, centerX + 10f, tutorialButtonY, 140f, 30f)) {
            val clientId = clientIdTextBox.getText()
            val clientSecret = clientSecretTextBox.getText()

            if (clientId.isEmpty() || clientSecret.isEmpty()) {
                setupError = true
                return
            }

            val musicManager = Shindo.getInstance().musicManager
            musicManager.saveCredentials(clientId, clientSecret)

            if (musicManager.hasCredentials()) {
                setupError = false
                showSetupScreen = false
                showConnectButton = true

                Shindo.getInstance().notificationManager.post(
                    TranslateText.SPOTIFY_AUTH,
                    TranslateText.CREDENTIALS_SAVED,
                    NotificationType.SUCCESS
                )
            } else {
                setupError = true
            }
        }
    }

    private fun handleDownloaderClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        textBox.mouseClicked(mouseX, mouseY, mouseButton)

        if (MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() - 34f, getY() + getHeight() - 80f, 18f, 18f) && mouseButton == 0) {
            openDownloader = false
            Shindo.getInstance().musicManager.play(textBox.getText())
            return
        }

        if (!MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() - 175f, getY() + getHeight() - 86f, 165f, 30f)) {
            openDownloader = false
        }
    }

    private fun handleControlBarClick(mouseX: Int, mouseY: Int) {
        val musicManager = Shindo.getInstance().musicManager
        val centerX = getX() + (getWidth() / 2f)
        val centerY = getY() + getHeight() - 32f

        if (MouseUtils.isInside(mouseX, mouseY, centerX - 24f - 8f, centerY, 16f, 16f)) {
            musicManager.previousTrack()
            return
        }

        if (MouseUtils.isInside(mouseX, mouseY, centerX - 8f, centerY, 16f, 16f)) {
            if (musicManager.isPlaying) {
                musicManager.pause()
            } else {
                musicManager.resume()
            }
            return
        }

        if (MouseUtils.isInside(mouseX, mouseY, centerX + 24f - 8f, centerY, 16f, 16f)) {
            musicManager.nextTrack()
            return
        }

        if (MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() - 72f, getY() + getHeight() - 22f, 62f, 8f)) {
            volumeSlider.mouseClicked(mouseX, mouseY, 0)
            return
        }

        handleProgressBarClick(mouseX, mouseY, musicManager)
    }

    private fun handleProgressBarClick(mouseX: Int, mouseY: Int, musicManager: MusicManager) {
        val progressBarY = getY() + getHeight() - 5
        if (MouseUtils.isInside(mouseX, mouseY, getX() + 20f, progressBarY - 5f, getWidth() - 40f, 10f)) {
            val clickPosition = (mouseX - (getX() + 20f)) / (getWidth() - 40f)
            musicManager.seekToPosition((clickPosition * trackDuration).toLong())
        }
    }

    private fun handleTrackClick(mouseX: Int, mouseY: Int) {
        var offsetY = 13f + scroll.getValue()
        val localResults = searchResults
        if (localResults != null) {
            for (track in localResults) {
                if (MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + offsetY, getWidth() - 30f, 46f)) {
                    if (MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() - 60f, getY() + offsetY + 15f, 16f, 16f)) {
                        addToQueue(track)
                    } else {
                        Shindo.getInstance().musicManager.play(track.uri)
                    }
                    return
                }
                offsetY += 56f
            }
        }
        val playlistResults = searchPlaylistResults
        if (playlistResults != null) {
            for (playlist in playlistResults) {
                if (playlist == null || playlist.uri == null) continue
                if (MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + offsetY, getWidth() - 30f, 46f)) {
                    try {
                        Shindo.getInstance().musicManager.playPlaylist(playlist.uri)
                    } catch (e: Exception) {
                        ShindoLogger.error("Failed to play playlist: " + e.message)
                        Shindo.getInstance().notificationManager.post(TranslateText.MUSIC, TranslateText.SPOTIFY_FAILED_TO_PLAY_PLAYLIST, NotificationType.ERROR)
                    }
                    return
                }
                offsetY += 56f
            }
        }
    }

    private fun handlePlaylistClick(mouseX: Int, mouseY: Int) {
        val playlists = userPlaylists ?: return
        val musicManager = Shindo.getInstance().musicManager

        var offsetY = 13f + (searchResults?.size ?: 0) * 56f + (searchPlaylistResults?.size ?: 0) * 56f + scroll.getValue()
        for (playlist in playlists) {
            if (playlist == null || playlist.uri == null) {
                continue
            }

            if (MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + offsetY, getWidth() - 30f, 46f)) {
                try {
                    musicManager.playPlaylist(playlist.uri)
                } catch (e: Exception) {
                    ShindoLogger.error("Failed to play playlist: " + e.message)
                    Shindo.getInstance().notificationManager.post(TranslateText.MUSIC, TranslateText.SPOTIFY_FAILED_TO_PLAY_PLAYLIST, NotificationType.ERROR)
                }
                break
            }
            offsetY += 56f
        }
    }

    private fun addToQueue(track: Track) {
        val musicManager = Shindo.getInstance().musicManager
        musicManager.addToQueue(track.uri)
            .thenRun { Shindo.getInstance().notificationManager.post(TranslateText.MUSIC, TranslateText.SPOTIFY_ADDED_TO_QUEUE, NotificationType.SUCCESS) }
            .exceptionally {
                Shindo.getInstance().notificationManager.post(TranslateText.MUSIC, TranslateText.SPOTIFY_FAILED_TO_ADD_TO_QUEUE, NotificationType.ERROR)
                null
            }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight().toFloat())) {
            return
        }

        if (!showSetupScreen) {
            volumeSlider.mouseReleased(mouseX, mouseY, mouseButton)
            updateVolume()
        }
    }

    private fun updateVolume() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastVolumeChangeTime > VOLUME_CHANGE_DELAY) {
            lastVolumeChangeTime = currentTime
            val volume = (volumeSlider.getSetting().getValueFloat() * 100).toInt()
            Shindo.getInstance().musicManager.setVolume(volume)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (showSetupScreen) {
            clientIdTextBox.keyTyped(typedChar, keyCode)
            clientSecretTextBox.keyTyped(typedChar, keyCode)
            return
        }

        if (openDownloader) {
            textBox.keyTyped(typedChar, keyCode)
        }

        val parent = parentRef.get()
        val searchBarFocused = parent != null && parent.getSearchBox() != null && parent.getSearchBox().isFocused()

        if (keyCode == Keyboard.KEY_SPACE && !showConnectButton && !searchBarFocused) {
            val musicManager = Shindo.getInstance().musicManager
            if (musicManager.isPlaying) {
                musicManager.pause()
            } else {
                musicManager.resume()
            }
        }

        if (keyCode == Keyboard.KEY_UP && !showConnectButton) {
            val musicManager = Shindo.getInstance().musicManager
            val currentVolume = (volumeSlider.getSetting().getValueFloat() * 100).toInt()
            val newVolume = kotlin.math.min(100, currentVolume + 5)
            volumeSlider.getSetting().setValue(newVolume / 100.0)
            musicManager.setVolume(newVolume)
            lastVolumeChangeTime = System.currentTimeMillis()
        } else if (keyCode == Keyboard.KEY_DOWN && !showConnectButton) {
            val musicManager = Shindo.getInstance().musicManager
            val currentVolume = (volumeSlider.getSetting().getValueFloat() * 100).toInt()
            val newVolume = kotlin.math.max(0, currentVolume - 5)
            volumeSlider.getSetting().setValue(newVolume / 100.0)
            musicManager.setVolume(newVolume)
            lastVolumeChangeTime = System.currentTimeMillis()
        }

        if (keyCode == Keyboard.KEY_RIGHT && !showConnectButton) {
            val musicManager = Shindo.getInstance().musicManager
            val newPosition = kotlin.math.min(trackPosition + 10000, trackDuration)
            musicManager.seekToPosition(newPosition)
        } else if (keyCode == Keyboard.KEY_LEFT && !showConnectButton) {
            val musicManager = Shindo.getInstance().musicManager
            val newPosition = kotlin.math.max(trackPosition - 10000, 0)
            musicManager.seekToPosition(newPosition)
        }

        if (showingLyrics) {
            lyricsScroll.onKey(keyCode)
        } else {
            scroll.onKey(keyCode)
        }
    }

    override fun onTrackInfoUpdated(position: Long, duration: Long) {
        trackPosition = position
        trackDuration = duration

        val musicManager = Shindo.getInstance().musicManager
        val currentTrack = musicManager.currentTrack
        if (currentTrack != null) {
            val trackId = currentTrack.id
            if (trackId != currentTrackId) {
                currentTrackId = trackId
                musicManager.getAlbumArtUrl(currentTrack)

                if (showingLyrics && musicManager.lyricsManager != null) {
                    musicManager.lyricsManager.fetchLyrics(currentTrack)
                    lyricsScroll.resetAll()
                }
            }
        } else {
            currentTrackId = null
        }
    }

    private fun updateScroll() {
        var totalResults = 0
        if (searchResults != null) totalResults += searchResults?.size ?: 0
        if (searchPlaylistResults != null) totalResults += searchPlaylistResults?.size ?: 0
        if (userPlaylists != null) totalResults += userPlaylists?.size ?: 0
        scroll.maxScroll = totalResults * 56f
    }

    private fun drawConnectButton(nvg: NanoVGManager, mouseX: Int, mouseY: Int) {
        val palette = Shindo.getInstance().colorManager.palette
        val accentColor = Shindo.getInstance().colorManager.currentColor

        val centerX = getX() + (getWidth() / 2f)
        val centerY = getY() + (getHeight() / 2f)

        val buttonWidth = 150f
        val buttonHeight = 40f
        val buttonX = centerX - (buttonWidth / 2f)
        val buttonY = centerY - (buttonHeight / 2f)

        val isHovered = MouseUtils.isInside(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight)
        nvg.drawRoundedRect(buttonX, buttonY, buttonWidth, buttonHeight, 8f,
            if (isHovered) accentColor.interpolateColor else palette.getBackgroundColor(ColorType.DARK))

        val text = TranslateText.SPOTIFY_CONNECT.text
        val textWidth = nvg.getTextWidth(text, 11f, Fonts.MEDIUM)
        val iconWidth = 16f
        val spacing = 8f
        val totalWidth = iconWidth + spacing + textWidth

        val startX = centerX - (totalWidth / 2f)

        val iconY = buttonY + (buttonHeight / 2f) - 8f

        val textY = buttonY + (buttonHeight / 2f) - 3f

        nvg.drawText(LegacyIcon.MUSIC, startX, iconY,
            if (isHovered) Color.WHITE else palette.getFontColor(ColorType.DARK), 16f, Fonts.LEGACYICON)

        nvg.drawText(text, startX + iconWidth + spacing, textY,
            if (isHovered) Color.WHITE else palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)

        if (DEBUG_HITBOXES) {
            nvg.drawRect(buttonX, buttonY, buttonWidth, buttonHeight, DEBUG_COLOR)
        }
    }

    private companion object {
        private const val VOLUME_CHANGE_DELAY = 500L
        private const val SEARCH_DEBOUNCE_DELAY = 800L
        private val PLACEHOLDER_IMAGE = ResourceLocation("shindo/music.png")

        private const val DEBUG_HITBOXES = false
        private val DEBUG_COLOR = Color(255, 0, 0, 100)
    }
}
