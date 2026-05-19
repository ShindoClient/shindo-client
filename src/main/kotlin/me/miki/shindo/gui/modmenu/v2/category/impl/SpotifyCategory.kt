package me.miki.shindo.gui.modmenu.v2.category.impl

import com.wrapper.spotify.model_objects.specification.PlaylistSimplified
import com.wrapper.spotify.model_objects.specification.Track
import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.v2.GuiModMenu
import me.miki.shindo.gui.modmenu.v2.category.Category
import me.miki.shindo.gui.modmenu.v2.render.ModMenuClipCoordinator
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.music.MusicManager
import me.miki.shindo.management.music.TrackInfoCallback
import me.miki.shindo.management.music.cache.AlbumArtCache
import me.miki.shindo.management.music.model.LyricsLine
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Font
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.notification.NotificationType
import me.miki.shindo.ui.components.v2.inputs.CompSlider
import me.miki.shindo.ui.components.v2.inputs.CompTextBox
import me.miki.shindo.utils.BrowserUtils
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiConfirmOpenLink
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

class SpotifyCategory(
    parent: GuiModMenu,
) : Category(parent, TranslateText.SPOTIFY, Lucide.MUSIC, true, true),
    TrackInfoCallback {
    private val volumeSlider =
        CompSlider(
            InternalSettingsMod.instance.getVolumeSetting()
                ?: throw IllegalStateException("Internal volume setting is not registered"),
        )
    private val textBox = CompTextBox()
    private val parentRef = WeakReference(parent)
    private val searchDebouncer: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "Search-Debouncer").apply { isDaemon = true }
        }
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
    private var trackPosition = 0L
    private var trackDuration = 0L
    private var lastVolumeChangeTime = 0L
    private var lastSearchQuery = ""
    private var currentTrackId: String? = null
    private var showConnectButton = true
    private var showingLyrics = false
    private var currentHighlightedLyricIndex = -1

    init {
        volumeSlider.setCircle(false)
        volumeSlider.setShowValue(false)
        textBox.setDefaultText("Enter a Spotify link")
    }

    override fun initGui() {}

    override fun initCategory() {
        scroll.resetAll()
        lyricsScroll.resetAll()
        showingLyrics = false

        val musicManager = Shindo.getInstance().getMusicManager()
        showConnectButton = !musicManager.isAuthorized()
        musicManager.setTrackInfoCallback(this)

        if (!showConnectButton) {
            fetchUserPlaylists()
            CompletableFuture.runAsync {
                try {
                    musicManager.fetchAndUpdateVolume()
                    volumeSlider.getSetting().setValue(musicManager.getVolume() / 100.0)
                } catch (e: Exception) {
                    ShindoLogger.warn("Failed to sync volume slider: ${e.message}")
                }
            }
        }
    }

    private fun fetchUserPlaylists() {
        Shindo
            .getInstance()
            .getMusicManager()
            .getUserPlaylists()
            .thenAccept { playlists ->
                if (playlists != null) Collections.reverse(playlists)
                userPlaylists = playlists
            }.exceptionally { ex ->
                ShindoLogger.error("Failed to fetch user playlists: ${ex.message}")
                null
            }
    }

    private fun openConfirmDialog(uri: String) {
        val mc = Minecraft.getMinecraft()
        val gui =
            GuiConfirmOpenLink({ result, _ ->
                if (result) tryOpenBrowser(uri)
                mc.displayGuiScreen(parentRef.get())
            }, uri, 0, true)
        gui.disableSecurityWarning()
        mc.displayGuiScreen(gui)
    }

    private fun tryOpenBrowser(uri: String) {
        try {
            BrowserUtils.tryOpenBrowser(uri)
        } catch (e: Exception) {
            Shindo
                .getInstance()
                .getNotificationManager()
                .post(TranslateText.SPOTIFY_AUTH, TranslateText.SPOTIFY_FAIL_BROWSER, NotificationType.ERROR)
            ShindoLogger.error(e.message!!)
        }
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager!!
        val palette = instance.getColorManager().getPalette()
        val accentColor = instance.getColorManager().getCurrentColor()
        val musicManager = instance.getMusicManager()

        if (!isSearching.get()) checkAndUpdateSearch()
        if (showingLyrics) {
            lyricsScroll.onScroll()
            lyricsScroll.onAnimation()
        }

        if (showConnectButton) {
            drawConnectButton(nvg, mouseX, mouseY)
        } else {
            nvg.save()
            try {
                if (showingLyrics) {
                    drawLyricsView(nvg, palette, accentColor, musicManager, mouseX, mouseY)
                } else {
                    nvg.save()
                    nvg.translate(0f, scroll.getValue())
                    drawSearchResults(nvg, palette, accentColor, mouseX, mouseY)
                    drawUserPlaylists(nvg, palette, accentColor, mouseX, mouseY)
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
                nvg.drawVerticalGradientRect(
                    getX() + 15f,
                    getY().toFloat(),
                    getWidth() - 30f,
                    12f,
                    palette.getBackgroundColor(ColorType.NORMAL),
                    noColour,
                )
                nvg.drawVerticalGradientRect(
                    getX() + 15f,
                    getY() + getHeight() - 58f,
                    getWidth() - 30f,
                    12f,
                    noColour,
                    palette.getBackgroundColor(ColorType.NORMAL),
                )
            }
        }

        updateScroll()
    }

    private fun drawConnectButton(
        nvg: NanoVGManager,
        mouseX: Int,
        mouseY: Int,
    ) {
        val palette = Shindo.getInstance().getColorManager().getPalette()
        val accentColor = Shindo.getInstance().getColorManager().getCurrentColor()
        val centerX = getX() + (getWidth() / 2f)
        val centerY = getY() + (getHeight() / 2f)
        val buttonWidth = 150f
        val buttonHeight = 40f
        val buttonX = centerX - (buttonWidth / 2f)
        val buttonY = centerY - (buttonHeight / 2f)
        val isHovered = MouseUtils.isInside(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight)

        nvg.drawRoundedRect(
            buttonX,
            buttonY,
            buttonWidth,
            buttonHeight,
            8f,
            if (isHovered) accentColor.getInterpolateColor() else palette.getBackgroundColor(ColorType.DARK),
        )

        val text = TranslateText.SPOTIFY_CONNECT.getText()
        val textWidth = nvg.getTextWidth(text, 11f, Fonts.MEDIUM)
        val iconWidth = 16f
        val spacing = 8f
        val startX = centerX - ((iconWidth + spacing + textWidth) / 2f)
        val color = if (isHovered) Color.WHITE else palette.getFontColor(ColorType.DARK)

        nvg.drawText(Lucide.MUSIC, startX, buttonY + (buttonHeight / 2f) - 8f, color, 16f, Fonts.LUCIDE)
        nvg.drawText(text, startX + iconWidth + spacing, buttonY + (buttonHeight / 2f) - 3f, color, 11f, Fonts.MEDIUM)
    }

    private fun drawSearchResults(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mouseX: Int,
        mouseY: Int,
    ) {
        if (searchResults == null && searchPlaylistResults == null) return
        var offsetY = 13f
        searchResults?.forEach { track ->
            if (offsetY + 46f >= -scroll.getValue() && offsetY <= -scroll.getValue() + getHeight()) {
                drawTrackEntry(nvg, palette, accentColor, track, offsetY, mouseX, mouseY)
            }
            offsetY += 56f
        }
        searchPlaylistResults?.forEach { playlist ->
            if (offsetY + 46f >= -scroll.getValue() && offsetY <= -scroll.getValue() + getHeight()) {
                drawPlaylistEntry(nvg, palette, accentColor, playlist, offsetY, mouseX, mouseY)
            }
            offsetY += 56f
        }
    }

    private fun drawTrackEntry(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        track: Track,
        offsetY: Float,
        mouseX: Int,
        mouseY: Int,
    ) {
        val hovered = isEntryHovered(mouseX, mouseY, offsetY)
        drawEntryShell(nvg, palette, accentColor, offsetY, hovered)

        // Get album art with proper null/placeholder checks
        val albumArtUrl = Shindo.getInstance().getMusicManager().getAlbumArtUrl(track)
        val isValidImage =
            !albumArtUrl.isNullOrBlank() &&
                albumArtUrl != AlbumArtCache.PLACEHOLDER_PATH &&
                File(albumArtUrl).exists()

        if (isValidImage) {
            try {
                nvg.drawRoundedImage(File(albumArtUrl!!), getX() + 20f, getY() + offsetY + 5f, 36f, 36f, 6f)
            } catch (e: Exception) {
                drawPlaceholderImage(nvg, offsetY)
            }
        } else {
            drawPlaceholderImage(nvg, offsetY)
        }

        val actionColor = if (hovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL)

        // Safe null checks for track data
        val safeName = track.name ?: "Unknown Track"
        val safeArtist = track.artists?.getOrNull(0)?.name ?: "Unknown Artist"

        nvg.drawText(
            nvg.getLimitText(safeName, 11f, Fonts.MEDIUM, 280f),
            getX() + 63f,
            getY() + offsetY + 9f,
            palette.getFontColor(ColorType.DARK),
            11f,
            Fonts.MEDIUM,
        )
        nvg.drawText(
            safeArtist,
            getX() + 63f,
            getY() + offsetY + 25f,
            palette.getFontColor(ColorType.NORMAL),
            9f,
            Fonts.MEDIUM,
        )
        nvg.drawText(
            Lucide.PLUS_SQUARE,
            getX() + getWidth() - 60f,
            getY() + offsetY + 15f,
            actionColor,
            16f,
            Fonts.LUCIDE,
        )
    }

    private fun drawPlaylistEntry(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        playlist: PlaylistSimplified,
        offsetY: Float,
        mouseX: Int,
        mouseY: Int,
    ) {
        val hovered = isEntryHovered(mouseX, mouseY, offsetY)
        drawEntryShell(nvg, palette, accentColor, offsetY, hovered)

        // Get image URL - returns cached path, placeholder marker, or null
        val imageUrl = Shindo.getInstance().getMusicManager().getPlaylistImageUrl(playlist)
        val isValidImage =
            !imageUrl.isNullOrBlank() &&
                imageUrl != AlbumArtCache.PLACEHOLDER_PATH &&
                File(imageUrl).exists()

        if (isValidImage) {
            try {
                nvg.drawRoundedImage(File(imageUrl!!), getX() + 20f, getY() + offsetY + 5f, 36f, 36f, 6f)
            } catch (e: Exception) {
                drawPlaceholderImage(nvg, offsetY)
            }
        } else {
            drawPlaceholderImage(nvg, offsetY)
        }

        // Safe null checks for playlist name and owner
        val safeName = playlist.name ?: "Untitled Playlist"
        val safeOwner = playlist.owner?.displayName ?: "Unknown Artist"

        nvg.drawText(
            nvg.getLimitText(safeName, 11f, Fonts.MEDIUM, 280f),
            getX() + 63f,
            getY() + offsetY + 9f,
            palette.getFontColor(ColorType.DARK),
            11f,
            Fonts.MEDIUM,
        )
        nvg.drawText(
            safeOwner,
            getX() + 63f,
            getY() + offsetY + 25f,
            palette.getFontColor(ColorType.NORMAL),
            9f,
            Fonts.MEDIUM,
        )
        nvg.drawText(
            Lucide.PLAY,
            getX() + getWidth() - 60f,
            getY() + offsetY + 15f,
            if (hovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
            16f,
            Fonts.LUCIDE,
        )
    }

    private fun drawUserPlaylists(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mouseX: Int,
        mouseY: Int,
    ) {
        val playlists = userPlaylists?.filterNotNull() ?: return
        var offsetY = 13f + (searchResults?.size ?: 0) * 56f + (searchPlaylistResults?.size ?: 0) * 56f
        for (playlist in playlists) {
            if (playlist == null) continue
            val hovered = isEntryHovered(mouseX, mouseY, offsetY)
            drawEntryShell(nvg, palette, accentColor, offsetY, hovered)
            val imageUrl = Shindo.getInstance().getMusicManager().getPlaylistImageUrl(playlist)
            val isValidImage =
                !imageUrl.isNullOrBlank() &&
                    imageUrl != AlbumArtCache.PLACEHOLDER_PATH &&
                    File(imageUrl).exists()
            if (isValidImage) {
                try {
                    nvg.drawRoundedImage(File(imageUrl), getX() + 20f, getY() + offsetY + 5f, 36f, 36f, 6f)
                } catch (e: Exception) {
                    drawPlaceholderImage(nvg, offsetY)
                }
            } else {
                drawPlaceholderImage(nvg, offsetY)
            }

            // Safe null checks
            val safeName = playlist.name ?: "Untitled Playlist"
            val safeOwner = playlist.owner?.displayName ?: "Unknown Owner"

            nvg.drawText(
                nvg.getLimitText(safeName, 11f, Fonts.MEDIUM, 280f),
                getX() + 63f,
                getY() + offsetY + 9f,
                palette.getFontColor(ColorType.DARK),
                11f,
                Fonts.MEDIUM,
            )
            nvg.drawText(
                safeOwner,
                getX() + 63f,
                getY() + offsetY + 25f,
                palette.getFontColor(ColorType.NORMAL),
                9f,
                Fonts.MEDIUM,
            )
            nvg.drawText(
                Lucide.PLAY,
                getX() + getWidth() - 60f,
                getY() + offsetY + 15f,
                if (hovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
                16f,
                Fonts.LUCIDE,
            )
            offsetY += 56f
        }
    }

    private fun drawPlaceholderImage(
        nvg: NanoVGManager,
        offsetY: Float,
    ) {
        try {
            nvg.drawRoundedImage(PLACEHOLDER_IMAGE, getX() + 20f, getY() + offsetY + 5f, 36f, 36f, 6f)
        } catch (e: Exception) {
            nvg.drawRoundedRect(getX() + 20f, getY() + offsetY + 5f, 36f, 36f, 6f, Color(50, 50, 50))
        }
    }

    private fun drawEntryShell(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        offsetY: Float,
        hovered: Boolean,
    ) {
        val x = getX() + 15f
        val y = getY() + offsetY
        val w = getWidth() - 30f
        val h = 46f

        nvg.drawShadow(x, y, w, h, 8f, 7)
        nvg.drawRoundedRect(x, y, w, h, 8f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 220))
        nvg.drawOutlineRoundedRect(
            x,
            y,
            w,
            h,
            8f,
            1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210),
        )
    }

    private fun isEntryHovered(
        mouseX: Int,
        mouseY: Int,
        offsetY: Float,
    ): Boolean =
        MouseUtils.isInside(
            mouseX,
            mouseY,
            getX() + 15f,
            getY() + offsetY + scroll.getValue(),
            getWidth() - 30f,
            46f,
        )

    private fun drawControlBar(
        nvg: NanoVGManager,
        palette: ColorPalette,
        musicManager: MusicManager,
    ) {
        nvg.drawRoundedRectVarying(
            getX().toFloat(),
            getY() + getHeight() - 46f,
            getWidth().toFloat(),
            46f,
            0f,
            0f,
            0f,
            12f,
            palette.getBackgroundColor(ColorType.DARK),
        )
        val currentTrack = musicManager.getCurrentTrack()
        val artY = getY() + getHeight() - 43f

        if (currentTrack != null) {
            // Get album art with proper checks
            val albumArtUrl = musicManager.getAlbumArtUrl(currentTrack)
            val isValidImage =
                !albumArtUrl.isNullOrBlank() &&
                    albumArtUrl != AlbumArtCache.PLACEHOLDER_PATH &&
                    File(albumArtUrl).exists()

            if (isValidImage) {
                try {
                    nvg.drawRoundedImage(File(albumArtUrl), getX() + 4f, artY, 36f, 36f, 6f)
                } catch (e: Exception) {
                    drawControlBarPlaceholder(nvg, artY)
                }
            } else {
                drawControlBarPlaceholder(nvg, artY)
            }

            // Safe null checks
            val safeName = currentTrack.name ?: "Unknown"
            val safeArtist = currentTrack.artists?.getOrNull(0)?.name ?: "Unknown"

            nvg.drawText(
                nvg.getLimitText(safeName, 9f, Fonts.MEDIUM, 100f),
                getX() + 45f,
                getY() + getHeight() - 39f,
                palette.getFontColor(ColorType.DARK),
                9f,
                Fonts.MEDIUM,
            )
            nvg.drawText(
                nvg.getLimitText(safeArtist, 9f, Fonts.MEDIUM, 100f),
                getX() + 45f,
                getY() + getHeight() - 27f,
                palette.getFontColor(ColorType.NORMAL),
                9f,
                Fonts.MEDIUM,
            )
        } else {
            drawControlBarPlaceholder(nvg, artY)
            nvg.drawText(
                TranslateText.NOTHING_IS_PLAYING.getText(),
                getX() + 45f,
                getY() + getHeight() - 33f,
                palette.getFontColor(ColorType.DARK),
                9f,
                Fonts.MEDIUM,
            )
        }
    }

    private fun drawControlBarPlaceholder(
        nvg: NanoVGManager,
        artY: Float,
    ) {
        nvg.drawRoundedRect(getX() + 4f, artY, 36f, 36f, 6f, Color(50, 50, 50))
        try {
            nvg.drawRoundedImage(PLACEHOLDER_IMAGE, getX() + 4f, artY, 36f, 36f, 6f)
        } catch (ignored: Exception) {
            // ignore
        }
    }

    private fun drawPlaybackControls(
        nvg: NanoVGManager,
        palette: ColorPalette,
        musicManager: MusicManager,
    ) {
        val centerX = getX() + (getWidth() / 2f)
        val centerY = getY() + getHeight() - 32f
        val color = palette.getFontColor(ColorType.NORMAL)
        nvg.drawText(Lucide.REWIND, centerX - 32f, centerY, color, 16f, Fonts.LUCIDE)
        nvg.drawText(
            if (musicManager.isPlaying()) Lucide.PAUSE else Lucide.PLAY,
            centerX - 8f,
            centerY,
            color,
            16f,
            Fonts.LUCIDE,
        )
        nvg.drawText(Lucide.FAST_FORWARD, centerX + 16f, centerY, color, 16f, Fonts.LUCIDE)
    }

    private fun drawVolumeSlider(
        nvg: NanoVGManager,
        palette: ColorPalette,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        volumeSlider.setX(getX() + getWidth() - 72f)
        volumeSlider.setY(getY() + getHeight() - 20f)
        volumeSlider.setWidth(62f)
        volumeSlider.setHeight(4.5f)
        volumeSlider.draw(mouseX, mouseY, partialTicks)
        val volume = (volumeSlider.getSetting().getValueFloat() * 100).toInt()
        val icon =
            when {
                volume == 0 -> Lucide.VOLUME_X
                volume > 80 -> Lucide.VOLUME_2
                volume > 40 -> Lucide.VOLUME_1
                else -> Lucide.VOLUME
            }
        nvg.drawText(
            icon,
            getX() + getWidth() - 94f,
            getY() + getHeight() - 26f,
            palette.getFontColor(ColorType.NORMAL),
            16f,
            Fonts.LUCIDE,
        )
    }

    private fun drawProgressBar(
        nvg: NanoVGManager,
        accentColor: AccentColor,
        palette: ColorPalette,
    ) {
        if (trackDuration <= 0) return
        val y = (getY() + getHeight() - 5).toFloat()
        val w = (getWidth() - 40).toFloat()
        nvg.drawRoundedRect(getX() + 20f, y, w, 2f, 1f, palette.getBackgroundColor(ColorType.NORMAL))
        nvg.drawRoundedRect(
            getX() + 20f,
            y,
            w * (trackPosition.toFloat() / trackDuration.toFloat()),
            2f,
            1f,
            accentColor.getInterpolateColor(),
        )
    }

    private fun drawLyricsButton(
        nvg: NanoVGManager,
        palette: ColorPalette,
        mouseX: Int,
        mouseY: Int,
    ) {
        val bx = getX() + getWidth() - 116f
        val by = getY() + getHeight() - 26f
        nvg.drawText(
            Lucide.LIST,
            bx,
            by,
            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    bx,
                    by,
                    16f,
                    16f,
                )
            ) {
                palette.getFontColor(ColorType.DARK)
            } else {
                palette.getFontColor(ColorType.NORMAL)
            },
            16f,
            Fonts.LUCIDE,
        )
    }

    private fun drawLyricsView(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        musicManager: MusicManager,
        mouseX: Int,
        mouseY: Int,
    ) {
        nvg.drawRoundedRect(
            getX().toFloat(),
            getY().toFloat(),
            getWidth().toFloat(),
            getHeight() - 46f,
            0f,
            palette.getBackgroundColor(ColorType.NORMAL),
        )
        val isBackHovered = MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + 15f, 16f, 16f)
        nvg.drawText(
            Lucide.ARROW_LEFT,
            getX() + 15f,
            getY() + 15f,
            if (isBackHovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
            16f,
            Fonts.LUCIDE,
        )
        if (musicManager.getCurrentTrack() != null) {
            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    getX().toFloat(),
                    getY().toFloat(),
                    getWidth().toFloat(),
                    getHeight() - 46f,
                )
            ) {
                lyricsScroll.onScroll()
            }
            lyricsScroll.onAnimation()
            drawScrollableLyrics(nvg, palette, accentColor, musicManager, mouseX, mouseY, 0f, trackPosition)
        } else {
            nvg.drawCenteredText(
                "No track is currently playing",
                getX() + (getWidth() / 2f),
                getY() + getHeight() / 2.7f,
                palette.getFontColor(ColorType.NORMAL),
                14f,
                Fonts.MEDIUM,
            )
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
        currentPosition: Long,
    ) {
        val lyricsManager = musicManager.getLyricsManager()
        val lyrics = lyricsManager.getCurrentLyrics()
        if (lyrics == null || lyrics.isError() || lyrics.lines.isEmpty()) {
            nvg.drawCenteredText(
                "No lyrics available for this track",
                getX() + (getWidth() / 2f),
                getY() + getHeight() / 2.7f,
                palette.getFontColor(ColorType.NORMAL),
                14f,
                Fonts.MEDIUM,
            )
            return
        }
        val allLines = lyrics.lines
        lyricsManager.updateCurrentLineIndex(currentPosition)
        val currentLineIndex = lyricsManager.getCurrentLineIndex()
        val lyricsAreaHeight = getHeight() - startY - 46f
        var totalContentHeight = 0f
        val yOffset = lyricsScroll.getValue()
        currentHighlightedLyricIndex = -1
        val lineHeights = IntArray(allLines.size)
        val wrappedLines = arrayOfNulls<Array<String>>(allLines.size)
        val maxTextWidth = getWidth() - 60f
        for (i in allLines.indices) {
            val text = extractLyricsText(allLines[i])
            if (text.isEmpty()) {
                lineHeights[i] = 30
                wrappedLines[i] = emptyArray()
                continue
            }
            val fontSize = if (i == currentLineIndex) 14f else 12f
            val wrapped = wrapText(nvg, text, fontSize, Fonts.MEDIUM, maxTextWidth)
            wrappedLines[i] = wrapped
            lineHeights[i] = if (wrapped.size <= 1) 30 else (fontSize * wrapped.size * 1.0f + 10).toInt()
            totalContentHeight += lineHeights[i].toFloat()
        }
        ModMenuClipCoordinator.withClip(
            nvg = nvg,
            x = getX() + 15f,
            y = getY() + startY,
            width = getWidth() - 30f,
            height = lyricsAreaHeight,
        ) {
            var currentY = getY() + startY + yOffset
            val visibleTop = getY() + startY
            val visibleBottom = visibleTop + lyricsAreaHeight
            for (i in allLines.indices) {
                if (currentY + lineHeights[i] < visibleTop || currentY > visibleBottom) {
                    currentY += lineHeights[i]
                    continue
                }
                val isCurrentLine = i == currentLineIndex
                val isHovered =
                    MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        getX() + 20f,
                        currentY,
                        getWidth() - 40f,
                        lineHeights[i].toFloat(),
                    )
                if (isHovered) currentHighlightedLyricIndex = i
                val lineColor: Color
                val fontSize: Float
                if (isCurrentLine) {
                    lineColor = accentColor.getInterpolateColor()
                    fontSize = 14f
                    nvg.drawRoundedRect(
                        getX() + 20f,
                        currentY,
                        getWidth() - 40f,
                        lineHeights[i].toFloat(),
                        4f,
                        Color(
                            accentColor.getColor1().red,
                            accentColor.getColor1().green,
                            accentColor.getColor1().blue,
                            30,
                        ),
                    )
                } else if (isHovered) {
                    lineColor = palette.getFontColor(ColorType.DARK)
                    fontSize = 12f
                } else {
                    lineColor = palette.getFontColor(ColorType.NORMAL)
                    fontSize = 12f
                }
                val wrapped = wrappedLines[i] ?: emptyArray()
                if (wrapped.isNotEmpty()) {
                    var wrapOffset = 0f
                    for (line in wrapped) {
                        val tx = getX() + (getWidth() / 2f) - (nvg.getTextWidth(line, fontSize, Fonts.MEDIUM) / 2f)
                        val ty = currentY + wrapOffset + (fontSize / 2f)
                        if (isCurrentLine) {
                            nvg.drawTextGlowing(line, tx, ty, lineColor, 8f, fontSize, Fonts.MEDIUM)
                        } else {
                            nvg.drawText(line, tx, ty, lineColor, fontSize, Fonts.MEDIUM)
                        }
                        wrapOffset += fontSize
                    }
                }
                currentY += lineHeights[i]
            }
        }
        val maxScroll = max(0f, totalContentHeight - lyricsAreaHeight + 20f)
        lyricsScroll.maxScroll = maxScroll
        if (lyricsScroll.getValue() < 0) {
            nvg.drawVerticalGradientRect(
                getX() + 15f,
                getY() + startY,
                getWidth() - 30f,
                12f,
                palette.getBackgroundColor(ColorType.NORMAL),
                noColour,
            )
        }
        if (-lyricsScroll.getValue() < maxScroll) {
            nvg.drawVerticalGradientRect(
                getX() + 15f,
                getY() + startY + lyricsAreaHeight - 12f,
                getWidth() - 30f,
                12f,
                noColour,
                palette.getBackgroundColor(ColorType.NORMAL),
            )
        }
    }

    private fun wrapText(
        nvg: NanoVGManager,
        text: String?,
        fontSize: Float,
        font: Font,
        maxWidth: Float,
    ): Array<String> {
        if (text == null || text.isEmpty()) return emptyArray()
        if (nvg.getTextWidth(text, fontSize, font) <= maxWidth) return arrayOf(text)
        val lines = ArrayList<String>()
        var currentLine = StringBuilder()
        for (word in text.split(" ")) {
            val testLine = if (currentLine.isNotEmpty()) "$currentLine $word" else word
            if (nvg.getTextWidth(testLine, fontSize, font) <= maxWidth) {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder()
                }
                if (nvg.getTextWidth(word, fontSize, font) > maxWidth) {
                    var partial = StringBuilder()
                    for (c in word) {
                        if (nvg.getTextWidth("$partial$c", fontSize, font) <= maxWidth) {
                            partial.append(c)
                        } else {
                            lines.add(partial.toString())
                            partial = StringBuilder().append(c)
                        }
                    }
                    if (partial.isNotEmpty()) currentLine = partial
                } else {
                    currentLine.append(word)
                }
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines.toTypedArray()
    }

    private fun extractLyricsText(line: LyricsLine): String {
        if (!line.words.isNullOrEmpty()) return line.words
        if (!line.romanizedWords.isNullOrEmpty()) return line.romanizedWords!!
        return ""
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (!MouseUtils.isInside(
                mouseX,
                mouseY,
                getX().toFloat(),
                getY().toFloat(),
                getWidth().toFloat(),
                getHeight().toFloat(),
            )
        ) {
            return
        }

        if (openDownloader) {
            handleDownloaderClick(mouseX, mouseY, mouseButton)
            return
        }

        if (showConnectButton) {
            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    getX() + (getWidth() / 2f) - 75f,
                    getY() + (getHeight() / 2f) - 20f,
                    150f,
                    40f,
                ) &&
                mouseButton == 0
            ) {
                openConfirmDialog(Shindo.getInstance().getMusicManager().getAuthorizationCodeUri())
                showConnectButton = false
            }
            return
        }

        if (!showingLyrics && mouseButton == 0) {
            val bx = getX() + getWidth() - 116f
            val by = getY() + getHeight() - 26f
            if (MouseUtils.isInside(mouseX, mouseY, bx, by, 16f, 16f)) {
                showingLyrics = true
                lyricsScroll.resetAll()
                val mm = Shindo.getInstance().getMusicManager()
                if (mm.getCurrentTrack() != null) mm.getLyricsManager().fetchLyrics(mm.getCurrentTrack())
                return
            }
        }

        if (mouseButton == 0 && mouseY >= getY() + getHeight() - 46) {
            handleControlBarClick(mouseX, mouseY)
            return
        }

        if (showingLyrics) {
            if (MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + 15f, 16f, 16f) && mouseButton == 0) {
                showingLyrics = false
                return
            }
            if (currentHighlightedLyricIndex >= 0 && mouseButton == 0) {
                val mm = Shindo.getInstance().getMusicManager()
                val lyrics = mm.getLyricsManager().getCurrentLyrics()
                if (lyrics != null && !lyrics.isError() && currentHighlightedLyricIndex < lyrics.lines.size) {
                    mm.seekToPosition(lyrics.lines[currentHighlightedLyricIndex].startTime)
                }
            }
            return
        }

        if (mouseButton == 0 && searchResults != null) {
            handleTrackClick(mouseX, mouseY)
        } else if (mouseButton == 0 && userPlaylists != null) {
            handlePlaylistClick(mouseX, mouseY)
        }
    }

    private fun handleDownloaderClick(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        textBox.mouseClicked(mouseX, mouseY, mouseButton)
        if (MouseUtils.isInside(
                mouseX,
                mouseY,
                getX() + getWidth() - 34f,
                getY() + getHeight() - 80f,
                18f,
                18f,
            ) &&
            mouseButton == 0
        ) {
            openDownloader = false
            Shindo.getInstance().getMusicManager().play(textBox.getText())
            return
        }
        if (!MouseUtils.isInside(
                mouseX,
                mouseY,
                getX() + getWidth() - 175f,
                getY() + getHeight() - 86f,
                165f,
                30f,
            )
        ) {
            openDownloader = false
        }
    }

    private fun handleControlBarClick(
        mouseX: Int,
        mouseY: Int,
    ) {
        val mm = Shindo.getInstance().getMusicManager()
        val cx = getX() + (getWidth() / 2f)
        val cy = getY() + getHeight() - 32f
        if (MouseUtils.isInside(mouseX, mouseY, cx - 32f, cy, 16f, 16f)) {
            mm.previousTrack()
            return
        }
        if (MouseUtils.isInside(mouseX, mouseY, cx - 8f, cy, 16f, 16f)) {
            if (mm.isPlaying()) mm.pause() else mm.resume()
            return
        }
        if (MouseUtils.isInside(mouseX, mouseY, cx + 16f, cy, 16f, 16f)) {
            mm.nextTrack()
            return
        }
        if (MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() - 72f, getY() + getHeight() - 22f, 62f, 8f)) {
            volumeSlider.mouseClicked(mouseX, mouseY, 0)
            return
        }
        val py = getY() + getHeight() - 5
        if (MouseUtils.isInside(mouseX, mouseY, getX() + 20f, py - 5f, getWidth() - 40f, 10f)) {
            mm.seekToPosition(((mouseX - (getX() + 20f)) / (getWidth() - 40f) * trackDuration).toLong())
        }
    }

    private fun handleTrackClick(
        mouseX: Int,
        mouseY: Int,
    ) {
        var offsetY = 13f + scroll.getValue()
        searchResults?.forEach { track ->
            if (MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + offsetY, getWidth() - 30f, 46f)) {
                if (MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        getX() + getWidth() - 60f,
                        getY() + offsetY + 15f,
                        16f,
                        16f,
                    )
                ) {
                    addToQueue(track)
                } else {
                    Shindo.getInstance().getMusicManager().play(track.uri)
                }
                return
            }
            offsetY += 56f
        }
        searchPlaylistResults?.forEach { playlist ->
            if (playlist.uri == null) return@forEach
            if (MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + offsetY, getWidth() - 30f, 46f)) {
                try {
                    Shindo.getInstance().getMusicManager().playPlaylist(playlist.uri)
                } catch (e: Exception) {
                    Shindo.getInstance().getNotificationManager().post(
                        TranslateText.MUSIC,
                        TranslateText.SPOTIFY_FAILED_TO_PLAY_PLAYLIST,
                        NotificationType.ERROR,
                    )
                }
                return
            }
            offsetY += 56f
        }
    }

    private fun handlePlaylistClick(
        mouseX: Int,
        mouseY: Int,
    ) {
        val playlists = userPlaylists ?: return
        var offsetY =
            13f + (searchResults?.size ?: 0) * 56f + (searchPlaylistResults?.size ?: 0) * 56f + scroll.getValue()
        for (playlist in playlists) {
            if (playlist.uri == null) {
                offsetY += 56f
                continue
            }
            if (MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + offsetY, getWidth() - 30f, 46f)) {
                try {
                    Shindo.getInstance().getMusicManager().playPlaylist(playlist.uri)
                } catch (e: Exception) {
                    Shindo.getInstance().getNotificationManager().post(
                        TranslateText.MUSIC,
                        TranslateText.SPOTIFY_FAILED_TO_PLAY_PLAYLIST,
                        NotificationType.ERROR,
                    )
                }
                break
            }
            offsetY += 56f
        }
    }

    private fun addToQueue(track: Track) {
        Shindo
            .getInstance()
            .getMusicManager()
            .addToQueue(track.uri)
            .thenRun {
                Shindo
                    .getInstance()
                    .getNotificationManager()
                    .post(TranslateText.MUSIC, TranslateText.SPOTIFY_ADDED_TO_QUEUE, NotificationType.SUCCESS)
            }.exceptionally {
                Shindo.getInstance().getNotificationManager().post(
                    TranslateText.MUSIC,
                    TranslateText.SPOTIFY_FAILED_TO_ADD_TO_QUEUE,
                    NotificationType.ERROR,
                )
                null
            }
    }

    override fun mouseReleased(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (!MouseUtils.isInside(
                mouseX,
                mouseY,
                getX().toFloat(),
                getY().toFloat(),
                getWidth().toFloat(),
                getHeight().toFloat(),
            )
        ) {
            return
        }
        if (!showConnectButton) {
            volumeSlider.mouseReleased(mouseX, mouseY, mouseButton)
            updateVolume()
        }
    }

    private fun updateVolume() {
        val now = System.currentTimeMillis()
        if (now - lastVolumeChangeTime > VOLUME_CHANGE_DELAY) {
            lastVolumeChangeTime = now
            Shindo.getInstance().getMusicManager().setVolume((volumeSlider.getSetting().getValueFloat() * 100).toInt())
        }
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        if (openDownloader) textBox.keyTyped(typedChar, keyCode)
        if (showConnectButton) return
        val mm = Shindo.getInstance().getMusicManager()
        val searchFocused = parentRef.get()?.getSearchBox()?.isFocused() == true
        if (keyCode == Keyboard.KEY_SPACE && !searchFocused) {
            if (mm.isPlaying()) mm.pause() else mm.resume()
        }
        if (keyCode == Keyboard.KEY_UP) {
            val v =
                min(100, (volumeSlider.getSetting().getValueFloat() * 100).toInt() + 5)
            volumeSlider
                .getSetting()
                .setValue(v / 100.0)
            mm.setVolume(v)
            lastVolumeChangeTime = System.currentTimeMillis()
        } else if (keyCode == Keyboard.KEY_DOWN) {
            val v =
                max(0, (volumeSlider.getSetting().getValueFloat() * 100).toInt() - 5)
            volumeSlider
                .getSetting()
                .setValue(v / 100.0)
            mm.setVolume(v)
            lastVolumeChangeTime = System.currentTimeMillis()
        }
        if (keyCode == Keyboard.KEY_RIGHT) {
            mm.seekToPosition(min(trackPosition + 10000, trackDuration))
        } else if (keyCode == Keyboard.KEY_LEFT) {
            mm.seekToPosition(max(trackPosition - 10000, 0))
        }
        if (showingLyrics) lyricsScroll.onKey(keyCode) else scroll.onKey(keyCode)
    }

    override fun onTrackInfoUpdated(
        position: Long,
        duration: Long,
    ) {
        trackPosition = position
        trackDuration = duration
        val mm = Shindo.getInstance().getMusicManager()
        val currentTrack = mm.getCurrentTrack()
        if (currentTrack != null) {
            if (currentTrack.id != currentTrackId) {
                currentTrackId = currentTrack.id
                mm.getAlbumArtUrl(currentTrack)
                if (showingLyrics) {
                    mm.getLyricsManager().fetchLyrics(currentTrack)
                    lyricsScroll.resetAll()
                }
            }
        } else {
            currentTrackId = null
        }
    }

    private fun updateScroll() {
        val totalItems = (searchResults?.size ?: 0) + (searchPlaylistResults?.size ?: 0) + (userPlaylists?.size ?: 0)
        val totalHeight = totalItems * 56f
        val visibleHeight = getHeight() - 100f // Reserve space for controls
        // maxScroll = total content - visible area (minimum 0)
        scroll.maxScroll = maxOf(0f, totalHeight - visibleHeight)
    }

    private fun checkAndUpdateSearch() {
        val parent = parentRef.get() ?: return
        val query = parent.getSearchBox().getText()
        if (query != lastSearchQuery) {
            scheduleSearch(query)
            lastSearchQuery = query
        }
    }

    private fun scheduleSearch(query: String) {
        if (query.isEmpty()) {
            searchResults = null
            searchPlaylistResults = null
            return
        }
        pendingSearch?.takeIf { !it.isDone }?.cancel(false)
        pendingSearch =
            searchDebouncer.schedule({
                if (isSearching.compareAndSet(false, true)) {
                    try {
                        val mm = Shindo.getInstance().getMusicManager()
                        searchResults = mm.searchTracks(query).join()
                        searchPlaylistResults = mm.searchPlaylists(query).join()
                        searchResults?.take(5)?.forEach { mm.getAlbumArtUrl(it) }
                        searchPlaylistResults?.take(5)?.forEach { mm.getPlaylistImageUrl(it) }
                    } catch (ex: Exception) {
                        ShindoLogger.error("Search failed", ex)
                        Shindo
                            .getInstance()
                            .getNotificationManager()
                            .post(TranslateText.MUSIC, TranslateText.SPOTIFY_SEARCH_FAILED, NotificationType.ERROR)
                    } finally {
                        isSearching.set(false)
                    }
                }
            }, SEARCH_DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)
    }

    private companion object {
        private const val VOLUME_CHANGE_DELAY = 500L
        private const val SEARCH_DEBOUNCE_DELAY = 800L
        private val PLACEHOLDER_IMAGE = ResourceLocation("shindo/music.png")
        private const val DEBUG_HITBOXES = false
        private val DEBUG_COLOR = Color(255, 0, 0, 100)
    }
}
