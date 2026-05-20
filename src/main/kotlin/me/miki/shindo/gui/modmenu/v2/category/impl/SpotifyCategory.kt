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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.min

// ─────────────────────────────────────────────────────────────────────────────
// Data holders
// ─────────────────────────────────────────────────────────────────────────────

/** Immutable snapshot of current search results, swapped atomically. */
private data class SearchSnapshot(
    val tracks: List<Track> = emptyList(),
    val playlists: List<PlaylistSimplified> = emptyList(),
)

// ─────────────────────────────────────────────────────────────────────────────
// SpotifyCategory
// ─────────────────────────────────────────────────────────────────────────────

class SpotifyCategory(
    parent: GuiModMenu,
) : Category(parent, TranslateText.SPOTIFY, Lucide.MUSIC, true, true),
    TrackInfoCallback {
    // ── Components ────────────────────────────────────────────────────────────

    private val volumeSlider =
        CompSlider(
            InternalSettingsMod.instance.getVolumeSetting()
                ?: error("Internal volume setting is not registered"),
        )
    private val textBox = CompTextBox()

    // ── References ────────────────────────────────────────────────────────────

    private val parentRef = WeakReference(parent)

    // ── Search state ──────────────────────────────────────────────────────────

    private val searchDebouncer: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "Search-Debouncer").apply { isDaemon = true }
        }

    /**
     * Atomic reference ensures the render thread always reads a fully-formed,
     * non-null snapshot. Background threads publish a new snapshot in a single
     * CAS, eliminating the NPE window that occurred when @Volatile vars were
     * nulled and re-assigned in separate writes.
     *
     * null  → no active search (show user playlists only)
     * non-null → display these results
     */
    private val searchSnapshot = AtomicReference<SearchSnapshot?>(null)

    private val isSearching = AtomicBoolean(false)
    private var pendingSearch: ScheduledFuture<*>? = null
    private var lastSearchQuery = ""

    // ── Playlist state ────────────────────────────────────────────────────────

    /**
     * User playlists are only written once (background fetch) and after that
     * only read from the render thread, so @Volatile is sufficient here.
     */
    @Volatile
    private var userPlaylists: List<PlaylistSimplified>? = null

    // ── Playback / UI state ───────────────────────────────────────────────────

    private val lyricsScroll = Scroll()
    private val noColor = Color(0, 0, 0, 0)

    private var openDownloader = false
    private var trackPosition = 0L
    private var trackDuration = 0L
    private var lastVolumeChangeTime = 0L
    private var currentTrackId: String? = null
    private var showConnectButton = true
    private var showingLyrics = false
    private var currentHighlightedLyricIndex = -1

    // ─────────────────────────────────────────────────────────────────────────
    // Init
    // ─────────────────────────────────────────────────────────────────────────

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
            syncVolumeSlider(musicManager)
        }
    }

    private fun syncVolumeSlider(musicManager: MusicManager) {
        CompletableFuture.runAsync {
            try {
                musicManager.fetchAndUpdateVolume()
                volumeSlider.getSetting().setValue(musicManager.getVolume() / 100.0)
            } catch (e: Exception) {
                ShindoLogger.warn("Failed to sync volume slider: ${e.message}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Playlist fetching
    // ─────────────────────────────────────────────────────────────────────────

    private fun fetchUserPlaylists() {
        Shindo
            .getInstance()
            .getMusicManager()
            .getUserPlaylists()
            .thenAccept { playlists ->
                userPlaylists = playlists.reversed()
            }.exceptionally { ex ->
                ShindoLogger.error("Failed to fetch user playlists: ${ex.message}")
                null
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Browser / auth helpers
    // ─────────────────────────────────────────────────────────────────────────

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
            Shindo.getInstance().getNotificationManager().post(
                TranslateText.SPOTIFY_AUTH,
                TranslateText.SPOTIFY_FAIL_BROWSER,
                NotificationType.ERROR,
            )
            ShindoLogger.error(e.message!!)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // drawScreen – main entry point
    // ─────────────────────────────────────────────────────────────────────────

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager
        val palette = instance.getColorManager().getPalette()
        val accentColor = instance.getColorManager().getCurrentColor()
        val musicManager = instance.getMusicManager()

        if (!isSearching.get()) checkAndUpdateSearch()

        if (showConnectButton) {
            drawConnectButton(nvg, mouseX, mouseY)
            return
        }

        nvg.save()
        try {
            if (showingLyrics) {
                lyricsScroll.onScroll()
                lyricsScroll.onAnimation()
                drawLyricsView(nvg, palette, accentColor, musicManager, mouseX, mouseY)
            } else {
                drawScrollableContent(nvg, palette, accentColor, musicManager, mouseX, mouseY, partialTicks)
            }
        } finally {
            nvg.restore()
        }

        updateScroll()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Connect button
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawConnectButton(
        nvg: NanoVGManager,
        mouseX: Int,
        mouseY: Int,
    ) {
        val palette = Shindo.getInstance().getColorManager().getPalette()
        val accentColor = Shindo.getInstance().getColorManager().getCurrentColor()

        val centerX = getX() + getWidth() / 2f
        val centerY = getY() + getHeight() / 2f
        val bw = 150f
        val bh = 40f
        val bx = centerX - bw / 2f
        val by = centerY - bh / 2f
        val hovered = MouseUtils.isInside(mouseX, mouseY, bx, by, bw, bh)

        nvg.drawRoundedRect(
            bx,
            by,
            bw,
            bh,
            8f,
            if (hovered) accentColor.getInterpolateColor() else palette.getBackgroundColor(ColorType.DARK),
        )

        val text = TranslateText.SPOTIFY_CONNECT.getText()
        val textWidth = nvg.getTextWidth(text, 11f, Fonts.MEDIUM)
        val iconWidth = 16f
        val spacing = 8f
        val startX = centerX - (iconWidth + spacing + textWidth) / 2f
        val color = if (hovered) Color.WHITE else palette.getFontColor(ColorType.DARK)

        nvg.drawText(Lucide.MUSIC, startX, by + bh / 2f - 8f, color, 16f, Fonts.LUCIDE)
        nvg.drawText(text, startX + iconWidth + spacing, by + bh / 2f - 3f, color, 11f, Fonts.MEDIUM)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Normal (non-lyrics) view
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawScrollableContent(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        musicManager: MusicManager,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        // Scrollable area
        nvg.save()
        nvg.translate(0f, scroll.getValue())

        // Take a single stable snapshot for the entire frame — prevents NPE
        // that occurred when background threads replaced @Volatile vars between
        // the null-check and the forEach call during fast scrolling.
        val snapshot = searchSnapshot.get()
        drawSearchResults(nvg, palette, accentColor, snapshot, mouseX, mouseY)
        drawUserPlaylists(nvg, palette, accentColor, snapshot, mouseX, mouseY)

        nvg.restore()

        // Fixed-position controls (not scrolled)
        drawControlBar(nvg, palette, musicManager)
        drawPlaybackControls(nvg, palette, musicManager)
        drawVolumeSlider(nvg, palette, mouseX, mouseY, partialTicks)
        drawLyricsButton(nvg, palette, mouseX, mouseY)
        drawProgressBar(nvg, accentColor, palette)

        // Fade edges
        nvg.drawVerticalGradientRect(
            getX() + 15f,
            getY().toFloat(),
            getWidth() - 30f,
            12f,
            palette.getBackgroundColor(ColorType.NORMAL),
            noColor,
        )
        nvg.drawVerticalGradientRect(
            getX() + 15f,
            getY() + getHeight() - 58f,
            getWidth() - 30f,
            12f,
            noColor,
            palette.getBackgroundColor(ColorType.NORMAL),
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Search results
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawSearchResults(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        snapshot: SearchSnapshot?,
        mouseX: Int,
        mouseY: Int,
    ) {
        snapshot ?: return

        var offsetY = 13f

        snapshot.tracks.forEach { track ->
            if (isEntryVisible(offsetY)) {
                drawTrackEntry(nvg, palette, accentColor, track, offsetY, mouseX, mouseY)
            }
            offsetY += ENTRY_HEIGHT
        }

        snapshot.playlists.forEach { playlist ->
            if (isEntryVisible(offsetY)) {
                drawPlaylistEntry(nvg, palette, accentColor, playlist, offsetY, mouseX, mouseY)
            }
            offsetY += ENTRY_HEIGHT
        }
    }

    private fun isEntryVisible(offsetY: Float): Boolean {
        val scrolled = -scroll.getValue()
        return offsetY + ENTRY_ITEM_HEIGHT >= scrolled && offsetY <= scrolled + getHeight()
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
        drawEntryArt(nvg, Shindo.getInstance().getMusicManager().getAlbumArtUrl(track), offsetY)

        val safeName = track.name ?: "Unknown Track"
        val safeArtist = track.artists?.getOrNull(0)?.name ?: "Unknown Artist"
        val actionColor = if (hovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL)

        drawEntryText(nvg, palette, safeName, safeArtist, offsetY)
        nvg.drawText(Lucide.PLUS_SQUARE, getX() + getWidth() - 60f, getY() + offsetY + 15f, actionColor, 16f, Fonts.LUCIDE)
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
        drawEntryArt(nvg, Shindo.getInstance().getMusicManager().getPlaylistImageUrl(playlist), offsetY)

        val safeName = playlist.name ?: "Untitled Playlist"
        val safeOwner = playlist.owner?.displayName ?: "Unknown Artist"

        drawEntryText(nvg, palette, safeName, safeOwner, offsetY)
        nvg.drawText(
            Lucide.PLAY,
            getX() + getWidth() - 60f,
            getY() + offsetY + 15f,
            if (hovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
            16f,
            Fonts.LUCIDE,
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // User playlists
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawUserPlaylists(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        snapshot: SearchSnapshot?,
        mouseX: Int,
        mouseY: Int,
    ) {
        val playlists = userPlaylists ?: return
        val searchTrackCount = snapshot?.tracks?.size ?: 0
        val searchPlaylistCount = snapshot?.playlists?.size ?: 0

        var offsetY = 13f + (searchTrackCount + searchPlaylistCount) * ENTRY_HEIGHT

        for (playlist in playlists) {
            val hovered = isEntryHovered(mouseX, mouseY, offsetY)
            drawEntryShell(nvg, palette, accentColor, offsetY, hovered)
            drawEntryArt(nvg, Shindo.getInstance().getMusicManager().getPlaylistImageUrl(playlist), offsetY)

            val safeName = playlist.name ?: "Untitled Playlist"
            val safeOwner = playlist.owner?.displayName ?: "Unknown Owner"

            drawEntryText(nvg, palette, safeName, safeOwner, offsetY)
            nvg.drawText(
                Lucide.PLAY,
                getX() + getWidth() - 60f,
                getY() + offsetY + 15f,
                if (hovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
                16f,
                Fonts.LUCIDE,
            )
            offsetY += ENTRY_HEIGHT
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shared entry drawing primitives
    // ─────────────────────────────────────────────────────────────────────────

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

        nvg.drawShadow(x, y, w, ENTRY_ITEM_HEIGHT, 8f, 7)
        nvg.drawRoundedRect(x, y, w, ENTRY_ITEM_HEIGHT, 8f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 220))
        nvg.drawOutlineRoundedRect(
            x,
            y,
            w,
            ENTRY_ITEM_HEIGHT,
            8f,
            1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210),
        )
    }

    private fun drawEntryArt(
        nvg: NanoVGManager,
        imageUrl: String?,
        offsetY: Float,
    ) {
        val artX = getX() + 20f
        val artY = getY() + offsetY + 5f
        val isValid =
            !imageUrl.isNullOrBlank() &&
                imageUrl != AlbumArtCache.PLACEHOLDER_PATH &&
                File(imageUrl).exists()

        if (isValid) {
            try {
                nvg.drawRoundedImage(File(imageUrl!!), artX, artY, 36f, 36f, 6f)
                return
            } catch (_: Exception) {
                // fall through to placeholder
            }
        }
        drawPlaceholderImage(nvg, offsetY)
    }

    private fun drawEntryText(
        nvg: NanoVGManager,
        palette: ColorPalette,
        name: String,
        subtitle: String,
        offsetY: Float,
    ) {
        nvg.drawText(
            nvg.getLimitText(name, 11f, Fonts.MEDIUM, 280f),
            getX() + 63f,
            getY() + offsetY + 9f,
            palette.getFontColor(ColorType.DARK),
            11f,
            Fonts.MEDIUM,
        )
        nvg.drawText(
            subtitle,
            getX() + 63f,
            getY() + offsetY + 25f,
            palette.getFontColor(ColorType.NORMAL),
            9f,
            Fonts.MEDIUM,
        )
    }

    private fun drawPlaceholderImage(
        nvg: NanoVGManager,
        offsetY: Float,
    ) {
        val artX = getX() + 20f
        val artY = getY() + offsetY + 5f
        try {
            nvg.drawRoundedImage(PLACEHOLDER_IMAGE, artX, artY, 36f, 36f, 6f)
        } catch (_: Exception) {
            nvg.drawRoundedRect(artX, artY, 36f, 36f, 6f, Color(50, 50, 50))
        }
    }

    private fun isEntryHovered(
        mouseX: Int,
        mouseY: Int,
        offsetY: Float,
    ): Boolean =
        MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + offsetY + scroll.getValue(), getWidth() - 30f, ENTRY_ITEM_HEIGHT)

    // ─────────────────────────────────────────────────────────────────────────
    // Control bar
    // ─────────────────────────────────────────────────────────────────────────

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
            drawEntryArt(nvg, musicManager.getAlbumArtUrl(currentTrack), artY - (getY() + 5f))
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
            nvg.drawRoundedRect(getX() + 4f, artY, 36f, 36f, 6f, Color(50, 50, 50))
            try {
                nvg.drawRoundedImage(PLACEHOLDER_IMAGE, getX() + 4f, artY, 36f, 36f, 6f)
            } catch (_: Exception) {
            }
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

    private fun drawPlaybackControls(
        nvg: NanoVGManager,
        palette: ColorPalette,
        musicManager: MusicManager,
    ) {
        val cx = getX() + getWidth() / 2f
        val cy = getY() + getHeight() - 32f
        val color = palette.getFontColor(ColorType.NORMAL)
        nvg.drawText(Lucide.REWIND, cx - 32f, cy, color, 16f, Fonts.LUCIDE)
        nvg.drawText(if (musicManager.isPlaying()) Lucide.PAUSE else Lucide.PLAY, cx - 8f, cy, color, 16f, Fonts.LUCIDE)
        nvg.drawText(Lucide.FAST_FORWARD, cx + 16f, cy, color, 16f, Fonts.LUCIDE)
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
        nvg.drawText(icon, getX() + getWidth() - 94f, getY() + getHeight() - 26f, palette.getFontColor(ColorType.NORMAL), 16f, Fonts.LUCIDE)
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
        nvg.drawRoundedRect(getX() + 20f, y, w * (trackPosition.toFloat() / trackDuration), 2f, 1f, accentColor.getInterpolateColor())
    }

    private fun drawLyricsButton(
        nvg: NanoVGManager,
        palette: ColorPalette,
        mouseX: Int,
        mouseY: Int,
    ) {
        val bx = getX() + getWidth() - 116f
        val by = getY() + getHeight() - 26f
        val hovered = MouseUtils.isInside(mouseX, mouseY, bx, by, 16f, 16f)
        nvg.drawText(
            Lucide.LIST,
            bx,
            by,
            if (hovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
            16f,
            Fonts.LUCIDE,
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lyrics view
    // ─────────────────────────────────────────────────────────────────────────

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
            if (MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight() - 46f)) {
                lyricsScroll.onScroll()
            }
            lyricsScroll.onAnimation()
            drawScrollableLyrics(nvg, palette, accentColor, musicManager, mouseX, mouseY, 0f, trackPosition)
        } else {
            nvg.drawCenteredText(
                "No track is currently playing",
                getX() + getWidth() / 2f,
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
                getX() + getWidth() / 2f,
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
        val yOffset = lyricsScroll.getValue()
        val maxTextWidth = getWidth() - 60f

        currentHighlightedLyricIndex = -1

        // Pre-compute wrapping for all lines to avoid per-frame recalculation
        val lineHeights = IntArray(allLines.size)
        val wrappedLines = arrayOfNulls<Array<String>>(allLines.size)
        var totalContentHeight = 0f

        for (i in allLines.indices) {
            val text = extractLyricsText(allLines[i])
            if (text.isEmpty()) {
                lineHeights[i] = 30
                wrappedLines[i] = emptyArray()
            } else {
                val fontSize = if (i == currentLineIndex) 14f else 12f
                val wrapped = wrapText(nvg, text, fontSize, Fonts.MEDIUM, maxTextWidth)
                wrappedLines[i] = wrapped
                lineHeights[i] = if (wrapped.size <= 1) 30 else (fontSize * wrapped.size * 1.0f + 10).toInt()
            }
            totalContentHeight += lineHeights[i]
        }

        ModMenuClipCoordinator.withClip(
            nvg = nvg,
            x = getX() + 15f,
            y = getY() + startY,
            width = getWidth() - 30f,
            height = lyricsAreaHeight,
        ) {
            val visibleTop = getY() + startY
            val visibleBottom = visibleTop + lyricsAreaHeight
            var currentY = visibleTop + yOffset

            for (i in allLines.indices) {
                val lineH = lineHeights[i].toFloat()

                if (currentY + lineH < visibleTop || currentY > visibleBottom) {
                    currentY += lineH
                    continue
                }

                val isCurrentLine = i == currentLineIndex
                val isHovered = MouseUtils.isInside(mouseX, mouseY, getX() + 20f, currentY, getWidth() - 40f, lineH)
                if (isHovered) currentHighlightedLyricIndex = i

                val (lineColor, fontSize) =
                    when {
                        isCurrentLine -> accentColor.getInterpolateColor() to 14f
                        isHovered -> palette.getFontColor(ColorType.DARK) to 12f
                        else -> palette.getFontColor(ColorType.NORMAL) to 12f
                    }

                if (isCurrentLine) {
                    nvg.drawRoundedRect(
                        getX() + 20f,
                        currentY,
                        getWidth() - 40f,
                        lineH,
                        4f,
                        Color(accentColor.getColor1().red, accentColor.getColor1().green, accentColor.getColor1().blue, 30),
                    )
                }

                val wrapped = wrappedLines[i] ?: emptyArray()
                var wrapOffset = 0f
                for (line in wrapped) {
                    val tx = getX() + getWidth() / 2f - nvg.getTextWidth(line, fontSize, Fonts.MEDIUM) / 2f
                    val ty = currentY + wrapOffset + fontSize / 2f
                    if (isCurrentLine) {
                        nvg.drawTextGlowing(line, tx, ty, lineColor, 8f, fontSize, Fonts.MEDIUM)
                    } else {
                        nvg.drawText(line, tx, ty, lineColor, fontSize, Fonts.MEDIUM)
                    }
                    wrapOffset += fontSize
                }

                currentY += lineH
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
                noColor,
            )
        }
        if (-lyricsScroll.getValue() < maxScroll) {
            nvg.drawVerticalGradientRect(
                getX() + 15f,
                getY() + startY + lyricsAreaHeight - 12f,
                getWidth() - 30f,
                12f,
                noColor,
                palette.getBackgroundColor(ColorType.NORMAL),
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Text utilities
    // ─────────────────────────────────────────────────────────────────────────

    private fun wrapText(
        nvg: NanoVGManager,
        text: String?,
        fontSize: Float,
        font: Font,
        maxWidth: Float,
    ): Array<String> {
        if (text.isNullOrEmpty()) return emptyArray()
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

    private fun extractLyricsText(line: LyricsLine): String =
        when {
            !line.words.isNullOrEmpty() -> line.words
            !line.romanizedWords.isNullOrEmpty() -> line.romanizedWords!!
            else -> ""
        }

    // ─────────────────────────────────────────────────────────────────────────
    // Mouse / keyboard input
    // ─────────────────────────────────────────────────────────────────────────

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (!MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight().toFloat())) return

        if (openDownloader) {
            handleDownloaderClick(mouseX, mouseY, mouseButton)
            return
        }

        if (showConnectButton) {
            handleConnectClick(mouseX, mouseY, mouseButton)
            return
        }

        if (!showingLyrics && mouseButton == 0) {
            val bx = getX() + getWidth() - 116f
            val by = getY() + getHeight() - 26f
            if (MouseUtils.isInside(mouseX, mouseY, bx, by, 16f, 16f)) {
                showingLyrics = true
                lyricsScroll.resetAll()
                val mm = Shindo.getInstance().getMusicManager()
                mm.getCurrentTrack()?.let { mm.getLyricsManager().fetchLyrics(it) }
                return
            }
        }

        if (mouseButton == 0 && mouseY >= getY() + getHeight() - 46) {
            handleControlBarClick(mouseX, mouseY)
            return
        }

        if (showingLyrics) {
            handleLyricsClick(mouseX, mouseY, mouseButton)
            return
        }

        if (mouseButton == 0) {
            // Take snapshot once so both handlers see the same data
            val snapshot = searchSnapshot.get()
            if (snapshot != null) {
                handleTrackClick(mouseX, mouseY, snapshot)
            } else {
                handlePlaylistClick(mouseX, mouseY)
            }
        }
    }

    private fun handleConnectClick(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton == 0 &&
            MouseUtils.isInside(
                mouseX,
                mouseY,
                getX() + getWidth() / 2f - 75f,
                getY() + getHeight() / 2f - 20f,
                150f,
                40f,
            )
        ) {
            openConfirmDialog(Shindo.getInstance().getMusicManager().getAuthorizationCodeUri())
            showConnectButton = false
        }
    }

    private fun handleLyricsClick(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
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
    }

    private fun handleDownloaderClick(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        textBox.mouseClicked(mouseX, mouseY, mouseButton)
        if (MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() - 34f, getY() + getHeight() - 80f, 18f, 18f) && mouseButton == 0) {
            openDownloader = false
            Shindo.getInstance().getMusicManager().play(textBox.getText())
            return
        }
        if (!MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() - 175f, getY() + getHeight() - 86f, 165f, 30f)) {
            openDownloader = false
        }
    }

    private fun handleControlBarClick(
        mouseX: Int,
        mouseY: Int,
    ) {
        val mm = Shindo.getInstance().getMusicManager()
        val cx = getX() + getWidth() / 2f
        val cy = getY() + getHeight() - 32f

        when {
            MouseUtils.isInside(mouseX, mouseY, cx - 32f, cy, 16f, 16f) -> {
                mm.previousTrack()
            }

            MouseUtils.isInside(mouseX, mouseY, cx - 8f, cy, 16f, 16f) -> {
                if (mm.isPlaying()) mm.pause() else mm.resume()
            }

            MouseUtils.isInside(mouseX, mouseY, cx + 16f, cy, 16f, 16f) -> {
                mm.nextTrack()
            }

            MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() - 72f, getY() + getHeight() - 22f, 62f, 8f) -> {
                volumeSlider.mouseClicked(mouseX, mouseY, 0)
            }

            else -> {
                val py = getY() + getHeight() - 5
                if (MouseUtils.isInside(mouseX, mouseY, getX() + 20f, py - 5f, getWidth() - 40f, 10f)) {
                    mm.seekToPosition(((mouseX - (getX() + 20f)) / (getWidth() - 40f) * trackDuration).toLong())
                }
            }
        }
    }

    private fun handleTrackClick(
        mouseX: Int,
        mouseY: Int,
        snapshot: SearchSnapshot,
    ) {
        var offsetY = 13f + scroll.getValue()

        snapshot.tracks.forEach { track ->
            if (MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + offsetY, getWidth() - 30f, ENTRY_ITEM_HEIGHT)) {
                if (MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() - 60f, getY() + offsetY + 15f, 16f, 16f)) {
                    addToQueue(track)
                } else {
                    Shindo.getInstance().getMusicManager().play(track.uri)
                }
                return
            }
            offsetY += ENTRY_HEIGHT
        }

        snapshot.playlists.forEach { playlist ->
            val uri =
                playlist.uri ?: run {
                    offsetY += ENTRY_HEIGHT
                    return@forEach
                }
            if (MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + offsetY, getWidth() - 30f, ENTRY_ITEM_HEIGHT)) {
                playPlaylistSafely(uri)
                return
            }
            offsetY += ENTRY_HEIGHT
        }
    }

    private fun handlePlaylistClick(
        mouseX: Int,
        mouseY: Int,
    ) {
        val playlists = userPlaylists ?: return
        val snapshot = searchSnapshot.get()
        val searchCount = (snapshot?.tracks?.size ?: 0) + (snapshot?.playlists?.size ?: 0)
        var offsetY = 13f + searchCount * ENTRY_HEIGHT + scroll.getValue()

        for (playlist in playlists) {
            val uri =
                playlist.uri ?: run {
                    offsetY += ENTRY_HEIGHT
                    continue
                }
            if (MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + offsetY, getWidth() - 30f, ENTRY_ITEM_HEIGHT)) {
                playPlaylistSafely(uri)
                break
            }
            offsetY += ENTRY_HEIGHT
        }
    }

    private fun playPlaylistSafely(uri: String) {
        try {
            Shindo.getInstance().getMusicManager().playPlaylist(uri)
        } catch (_: Exception) {
            Shindo.getInstance().getNotificationManager().post(
                TranslateText.MUSIC,
                TranslateText.SPOTIFY_FAILED_TO_PLAY_PLAYLIST,
                NotificationType.ERROR,
            )
        }
    }

    private fun addToQueue(track: Track) {
        Shindo
            .getInstance()
            .getMusicManager()
            .addToQueue(track.uri)
            .thenRun {
                Shindo.getInstance().getNotificationManager().post(
                    TranslateText.MUSIC,
                    TranslateText.SPOTIFY_ADDED_TO_QUEUE,
                    NotificationType.SUCCESS,
                )
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
        if (!MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight().toFloat())) return
        if (!showConnectButton) {
            volumeSlider.mouseReleased(mouseX, mouseY, mouseButton)
            updateVolume()
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

        when (keyCode) {
            Keyboard.KEY_UP -> adjustVolume(mm, +5)
            Keyboard.KEY_DOWN -> adjustVolume(mm, -5)
            Keyboard.KEY_RIGHT -> mm.seekToPosition(min(trackPosition + 10_000, trackDuration))
            Keyboard.KEY_LEFT -> mm.seekToPosition(max(trackPosition - 10_000, 0))
        }

        if (showingLyrics) lyricsScroll.onKey(keyCode) else scroll.onKey(keyCode)
    }

    private fun adjustVolume(
        mm: MusicManager,
        delta: Int,
    ) {
        val v = (volumeSlider.getSetting().getValueFloat() * 100).toInt().plus(delta).coerceIn(0, 100)
        volumeSlider.getSetting().setValue(v / 100.0)
        mm.setVolume(v)
        lastVolumeChangeTime = System.currentTimeMillis()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TrackInfoCallback
    // ─────────────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────────────
    // Scroll management
    // ─────────────────────────────────────────────────────────────────────────

    private fun updateScroll() {
        val snapshot = searchSnapshot.get()
        val searchCount = (snapshot?.tracks?.size ?: 0) + (snapshot?.playlists?.size ?: 0)
        val totalItems = searchCount + (userPlaylists?.size ?: 0)
        val totalHeight = totalItems * ENTRY_HEIGHT
        val visibleHeight = getHeight() - 100f
        scroll.maxScroll = maxOf(0f, totalHeight - visibleHeight)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Search / debounce
    // ─────────────────────────────────────────────────────────────────────────

    private fun checkAndUpdateSearch() {
        val query = parentRef.get()?.getSearchBox()?.getText() ?: return
        if (query != lastSearchQuery) {
            scheduleSearch(query)
            lastSearchQuery = query
        }
    }

    private fun scheduleSearch(query: String) {
        if (query.isEmpty()) {
            // Atomically clear — render thread will see null immediately
            searchSnapshot.set(null)
            return
        }

        pendingSearch?.takeIf { !it.isDone }?.cancel(false)

        pendingSearch =
            searchDebouncer.schedule({
                if (!isSearching.compareAndSet(false, true)) return@schedule
                try {
                    val mm = Shindo.getInstance().getMusicManager()
                    val tracks = mm.searchTracks(query).join() ?: emptyList()
                    val playlists = mm.searchPlaylists(query).join() ?: emptyList()

                    // Pre-fetch art while still on background thread
                    tracks.take(5).forEach { mm.getAlbumArtUrl(it) }
                    playlists.take(5).forEach { mm.getPlaylistImageUrl(it) }

                    // Single atomic publish — render thread sees complete snapshot or nothing
                    searchSnapshot.set(SearchSnapshot(tracks, playlists))
                } catch (ex: Exception) {
                    ShindoLogger.error("Search failed", ex)
                    Shindo.getInstance().getNotificationManager().post(
                        TranslateText.MUSIC,
                        TranslateText.SPOTIFY_SEARCH_FAILED,
                        NotificationType.ERROR,
                    )
                } finally {
                    isSearching.set(false)
                }
            }, SEARCH_DEBOUNCE_DELAY_MS, TimeUnit.MILLISECONDS)
    }

    private fun updateVolume() {
        val now = System.currentTimeMillis()
        if (now - lastVolumeChangeTime > VOLUME_CHANGE_DELAY_MS) {
            lastVolumeChangeTime = now
            Shindo.getInstance().getMusicManager().setVolume((volumeSlider.getSetting().getValueFloat() * 100).toInt())
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────────────────────────────────────

    private companion object {
        private const val VOLUME_CHANGE_DELAY_MS = 500L
        private const val SEARCH_DEBOUNCE_DELAY_MS = 800L
        private const val ENTRY_HEIGHT = 56f // total row spacing
        private const val ENTRY_ITEM_HEIGHT = 46f // visual card height
        private val PLACEHOLDER_IMAGE = ResourceLocation("shindo/music.png")
    }
}
