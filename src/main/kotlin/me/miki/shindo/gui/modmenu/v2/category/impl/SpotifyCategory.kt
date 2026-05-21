package me.miki.shindo.gui.modmenu.v2.category.impl

import com.wrapper.spotify.model_objects.specification.ArtistSimplified
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified
import com.wrapper.spotify.model_objects.specification.Track
import me.miki.extensions.music.getAlbumTracks
import me.miki.extensions.music.getArtistContent
import me.miki.extensions.music.getPlaylistTracks
import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.v2.GuiModMenu
import me.miki.shindo.gui.modmenu.v2.category.Category
import me.miki.shindo.gui.modmenu.v2.category.impl.spotify.ContentState
import me.miki.shindo.gui.modmenu.v2.category.impl.spotify.SpotifyNavigator
import me.miki.shindo.gui.modmenu.v2.category.impl.spotify.SpotifyScreen
import me.miki.shindo.gui.modmenu.v2.category.impl.spotify.data.ArtistContent
import me.miki.shindo.gui.modmenu.v2.category.impl.spotify.data.SearchSnapshot
import me.miki.shindo.gui.modmenu.v2.category.impl.spotify.data.TrackListContent
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

private const val SIDEBAR_WIDTH = 140f
private const val ENTRY_HEIGHT = 56f
private const val ENTRY_ITEM_H = 46f
private const val CONTROL_BAR_H = 46f
private const val TRACK_ROW_H = 38f
private const val TRACK_ROW_SPACING = 42f
private const val HEADER_H = 110f
private const val SIDEBAR_ROW_H = 34f
private const val SIDEBAR_ROW_SPACING = 38f

class SpotifyCategory(
    parent: GuiModMenu,
) : Category(parent, TranslateText.SPOTIFY, Lucide.MUSIC, true, true),
    TrackInfoCallback {
    private val volumeSlider =
        CompSlider(
            InternalSettingsMod.instance.getVolumeSetting()
                ?: error("Internal volume setting is not registered"),
        )

    private val parentRef = WeakReference(parent)

    private val navigator = SpotifyNavigator()

    private val searchDebouncer: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "Search-Debouncer").apply { isDaemon = true }
        }
    private val isSearching = AtomicBoolean(false)
    private val searchSnapshot = AtomicReference<SearchSnapshot?>(null)
    private var pendingSearch: ScheduledFuture<*>? = null
    private var lastSearchQuery = ""

    @Volatile private var userPlaylists: List<PlaylistSimplified>? = null

    private val trackListState = AtomicReference<ContentState<TrackListContent>>(ContentState.Idle)
    private val artistState = AtomicReference<ContentState<ArtistContent>>(ContentState.Idle)

    private val libraryScroll = Scroll()
    private val sidebarScroll = Scroll()
    private val detailScroll = Scroll()
    private val lyricsScroll = Scroll()

    private val noColor = Color(0, 0, 0, 0)
    private var trackPosition = 0L
    private var trackDuration = 0L
    private var lastVolumeChangeTime = 0L
    private var currentTrackId: String? = null
    private var showConnectButton = true
    private var currentHighlightedLyricIndex = -1

    init {
        volumeSlider.setCircle(false)
        volumeSlider.setShowValue(false)
    }

    override fun initGui() {}

    override fun initCategory() {
        navigator.reset()
        listOf(libraryScroll, sidebarScroll, detailScroll, lyricsScroll).forEach { it.resetAll() }
        trackListState.set(ContentState.Idle)
        artistState.set(ContentState.Idle)

        val mm = Shindo.getInstance().getMusicManager()
        showConnectButton = !mm.isAuthorized()
        mm.setTrackInfoCallback(this)

        if (!showConnectButton) {
            fetchUserPlaylists()
            syncVolumeAsync(mm)
        }
    }

    private fun syncVolumeAsync(mm: MusicManager) {
        CompletableFuture.runAsync {
            try {
                mm.fetchAndUpdateVolume()
                volumeSlider.getSetting().setValue(mm.getVolume() / 100.0)
            } catch (e: Exception) {
                ShindoLogger.warn("Volume sync failed: ${e.message}")
            }
        }
    }

    private fun fetchUserPlaylists() {
        Shindo
            .getInstance()
            .getMusicManager()
            .getUserPlaylists()
            .thenAccept { playlists -> userPlaylists = playlists?.reversed() }
            .exceptionally { ex ->
                ShindoLogger.error("Failed to fetch user playlists: ${ex.message}")
                null
            }
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager
        val palette = instance.getColorManager().getPalette()
        val accentColor = instance.getColorManager().getCurrentColor()
        val mm = instance.getMusicManager()

        if (!isSearching.get()) checkAndUpdateSearch()

        if (showConnectButton) {
            drawConnectButton(nvg, palette, accentColor, mouseX, mouseY)
            return
        }

        nvg.save()
        try {
            when (val screen = navigator.current) {
                is SpotifyScreen.Library -> {
                    drawLibraryLayout(nvg, palette, accentColor, mm, mouseX, mouseY, partialTicks)
                }

                is SpotifyScreen.Lyrics -> {
                    lyricsScroll.onScroll()
                    lyricsScroll.onAnimation()
                    drawLyricsView(nvg, palette, accentColor, mm, mouseX, mouseY)
                }

                is SpotifyScreen.PlaylistDetail -> {
                    detailScroll.onScroll()
                    detailScroll.onAnimation()
                    drawPlaylistDetailScreen(nvg, palette, accentColor, mm, screen, mouseX, mouseY)
                }

                is SpotifyScreen.ArtistDetail -> {
                    detailScroll.onScroll()
                    detailScroll.onAnimation()
                    drawArtistDetailScreen(nvg, palette, accentColor, mm, screen, mouseX, mouseY)
                }

                is SpotifyScreen.AlbumDetail -> {
                    detailScroll.onScroll()
                    detailScroll.onAnimation()
                    drawAlbumDetailScreen(nvg, palette, accentColor, mm, screen, mouseX, mouseY)
                }
            }
        } finally {
            nvg.restore()
        }
    }

    private fun drawConnectButton(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mouseX: Int,
        mouseY: Int,
    ) {
        val cx = getX() + getWidth() / 2f
        val cy = getY() + getHeight() / 2f
        val bw = 150f
        val bh = 40f
        val bx = cx - bw / 2f
        val by = cy - bh / 2f
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
        val tw = nvg.getTextWidth(text, 11f, Fonts.MEDIUM)
        val startX = cx - (16f + 8f + tw) / 2f
        val col = if (hovered) Color.WHITE else palette.getFontColor(ColorType.DARK)
        nvg.drawText(Lucide.MUSIC, startX, by + bh / 2f - 8f, col, 16f, Fonts.LUCIDE)
        nvg.drawText(text, startX + 24f, by + bh / 2f - 3f, col, 11f, Fonts.MEDIUM)
    }

    private fun drawLibraryLayout(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mm: MusicManager,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val mainX = getX() + SIDEBAR_WIDTH
        val mainW = getWidth() - SIDEBAR_WIDTH

        nvg.drawRoundedRectVarying(
            getX().toFloat(),
            getY().toFloat(),
            SIDEBAR_WIDTH,
            getHeight().toFloat(),
            12f,
            0f,
            0f,
            12f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 200),
        )
        nvg.drawRect(
            getX() + SIDEBAR_WIDTH - 1f,
            getY().toFloat(),
            1f,
            getHeight().toFloat(),
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 120),
        )

        sidebarScroll.onScroll()
        sidebarScroll.onAnimation()
        drawSidebar(nvg, palette, accentColor, mouseX, mouseY)

        libraryScroll.onScroll()
        libraryScroll.onAnimation()

        val snapshot = searchSnapshot.get()
        drawMainArea(nvg, palette, accentColor, mm, snapshot, mainX, mainW, mouseX, mouseY)

        drawControlBar(nvg, palette, mm)
        drawPlaybackControls(nvg, palette, mm)
        drawVolumeSlider(nvg, palette, mouseX, mouseY, partialTicks)
        drawLyricsButton(nvg, palette, mouseX, mouseY)
        drawProgressBar(nvg, accentColor, palette)

        nvg.drawVerticalGradientRect(mainX, getY().toFloat(), mainW, 12f, palette.getBackgroundColor(ColorType.NORMAL), noColor)
        nvg.drawVerticalGradientRect(
            mainX,
            getY() + getHeight() - CONTROL_BAR_H - 12f,
            mainW,
            12f,
            noColor,
            palette.getBackgroundColor(ColorType.NORMAL),
        )

        updateLibraryScroll(snapshot)
    }

    private fun drawSidebar(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mouseX: Int,
        mouseY: Int,
    ) {
        val playlists = userPlaylists ?: return
        val sx = getX() + 10f
        val sw = SIDEBAR_WIDTH - 20f

        nvg.drawText(Lucide.LIBRARY, sx, getY() + 16f, palette.getFontColor(ColorType.NORMAL), 14f, Fonts.LUCIDE)
        nvg.drawText("Your Library", sx + 20f, getY() + 20f, palette.getFontColor(ColorType.DARK), 9f, Fonts.MEDIUM)
        nvg.drawRect(sx, getY() + 34f, sw, 1f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 150))

        val clipTop = getY() + 38f
        val clipH = getHeight() - 38f - CONTROL_BAR_H

        ModMenuClipCoordinator.withClip(nvg = nvg, x = getX().toFloat(), y = clipTop, width = SIDEBAR_WIDTH, height = clipH) {
            var rowY = clipTop + sidebarScroll.getValue() + 4f
            playlists.forEach { playlist ->
                val inView = rowY + SIDEBAR_ROW_H > clipTop && rowY < clipTop + clipH
                if (inView) {
                    val hovered = MouseUtils.isInside(mouseX, mouseY, sx, rowY, sw, SIDEBAR_ROW_H)
                    val isActive = (navigator.current as? SpotifyScreen.PlaylistDetail)?.playlist?.id == playlist.id
                    if (hovered || isActive) {
                        nvg.drawRoundedRect(
                            sx - 2f,
                            rowY,
                            sw + 4f,
                            SIDEBAR_ROW_H,
                            6f,
                            if (isActive) {
                                ColorUtils.applyAlpha(accentColor.getInterpolateColor(), 30)
                            } else {
                                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 100)
                            },
                        )
                    }
                    drawSmallArt(nvg, Shindo.getInstance().getMusicManager().getPlaylistImageUrl(playlist), sx, rowY + 5f, 24f, 4f)
                    val nameColor = if (isActive) accentColor.getInterpolateColor() else palette.getFontColor(ColorType.DARK)
                    nvg.drawText(
                        nvg.getLimitText(playlist.name ?: "Untitled", 9f, Fonts.MEDIUM, sw - 34f),
                        sx + 30f,
                        rowY + 10f,
                        nameColor,
                        9f,
                        Fonts.MEDIUM,
                    )
                    nvg.drawText(
                        nvg.getLimitText(playlist.owner?.displayName ?: "", 8f, Fonts.REGULAR, sw - 34f),
                        sx + 30f,
                        rowY + 22f,
                        palette.getFontColor(ColorType.NORMAL),
                        8f,
                        Fonts.REGULAR,
                    )
                }
                rowY += SIDEBAR_ROW_SPACING
            }
        }
        sidebarScroll.maxScroll = max(0f, playlists.size * SIDEBAR_ROW_SPACING - clipH + 8f)
    }

    private fun drawMainArea(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mm: MusicManager,
        snapshot: SearchSnapshot?,
        mainX: Float,
        mainW: Float,
        mouseX: Int,
        mouseY: Int,
    ) {
        val clipTop = getY().toFloat()
        val clipH = getHeight() - CONTROL_BAR_H

        ModMenuClipCoordinator.withClip(nvg = nvg, x = mainX, y = clipTop, width = mainW, height = clipH) {
            nvg.save()
            nvg.translate(0f, libraryScroll.getValue())
            if (snapshot != null) {
                var offsetY = 13f
                snapshot.tracks.forEach { track ->
                    if (isEntryVisible(offsetY, clipTop, clipH)) {
                        drawTrackEntry(nvg, palette, accentColor, track, mainX + 8f, mainW - 16f, offsetY, mouseX, mouseY)
                    }
                    offsetY += ENTRY_HEIGHT
                }
                snapshot.playlists.forEach { playlist ->
                    if (isEntryVisible(offsetY, clipTop, clipH)) {
                        drawPlaylistEntry(nvg, palette, accentColor, playlist, mainX + 8f, mainW - 16f, offsetY, mouseX, mouseY)
                    }
                    offsetY += ENTRY_HEIGHT
                }
            } else {
                drawEmptyMainHint(nvg, palette, mainX, mainW, clipH)
            }
            nvg.restore()
        }
    }

    private fun drawEmptyMainHint(
        nvg: NanoVGManager,
        palette: ColorPalette,
        mainX: Float,
        mainW: Float,
        clipH: Float,
    ) {
        val cx = mainX + mainW / 2f
        val cy = getY() + clipH / 2.5f
        nvg.drawText(Lucide.SEARCH, cx - 8f, cy - 28f, palette.getFontColor(ColorType.NORMAL), 20f, Fonts.LUCIDE)
        nvg.drawCenteredText("Search for music above", cx, cy, palette.getFontColor(ColorType.NORMAL), 10f, Fonts.MEDIUM)
        nvg.drawCenteredText(
            "or select a playlist from the sidebar",
            cx,
            cy + 14f,
            palette.getFontColor(ColorType.NORMAL),
            9f,
            Fonts.REGULAR,
        )
    }

    private fun drawPlaylistDetailScreen(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mm: MusicManager,
        screen: SpotifyScreen.PlaylistDetail,
        mouseX: Int,
        mouseY: Int,
    ) {
        drawDetailHeader(
            nvg,
            palette,
            accentColor,
            mouseX,
            mouseY,
            imageUrl = mm.getPlaylistImageUrl(screen.playlist),
            title = screen.playlist.name ?: "Untitled Playlist",
            subtitle = "by ${screen.playlist.owner?.displayName ?: "Unknown"} · ${screen.playlist.tracks?.total ?: 0} tracks",
            onPlay = { mm.playPlaylist(screen.playlist.uri ?: return@drawDetailHeader) },
        )
        drawTrackListBody(nvg, palette, accentColor, mm, trackListState.get(), mouseX, mouseY, showArtist = true, showAlbum = true)
        drawSharedBottomControls(nvg, palette, accentColor, mm, mouseX, mouseY)
    }

    private fun drawArtistDetailScreen(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mm: MusicManager,
        screen: SpotifyScreen.ArtistDetail,
        mouseX: Int,
        mouseY: Int,
    ) {
        val state = artistState.get()
        val imageUrl = (state as? ContentState.Ready)?.data?.imageUrl
        val followerText = (state as? ContentState.Ready)?.data?.followerCount?.let { formatFollowers(it) } ?: ""
        val genre = (state as? ContentState.Ready)?.data?.genres?.firstOrNull() ?: ""

        drawDetailHeader(
            nvg,
            palette,
            accentColor,
            mouseX,
            mouseY,
            imageUrl = imageUrl,
            title = screen.artist.name ?: "Unknown Artist",
            subtitle =
                buildString {
                    if (followerText.isNotEmpty()) append("$followerText followers")
                    if (genre.isNotEmpty()) append(" · $genre")
                },
            isCircleArt = true,
        )
        when (state) {
            ContentState.Idle, is ContentState.Loading -> {
                drawLoadingSpinner(nvg, palette)
            }

            is ContentState.Error -> {
                drawErrorMessage(nvg, palette, state.message)
            }

            is ContentState.Ready -> {
                val wrapped = ContentState.Ready(TrackListContent(tracks = state.data.topTracks, totalCount = state.data.topTracks.size))
                drawTrackListBody(nvg, palette, accentColor, mm, wrapped, mouseX, mouseY, showArtist = false, showAlbum = true)
            }
        }
        drawSharedBottomControls(nvg, palette, accentColor, mm, mouseX, mouseY)
    }

    private fun drawAlbumDetailScreen(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mm: MusicManager,
        screen: SpotifyScreen.AlbumDetail,
        mouseX: Int,
        mouseY: Int,
    ) {
        val artUrl =
            screen.album.images
                ?.firstOrNull()
                ?.url
                ?.let { mm.getAlbumArt(it) }
        drawDetailHeader(
            nvg,
            palette,
            accentColor,
            mouseX,
            mouseY,
            imageUrl = artUrl,
            title = screen.album.name ?: "Unknown Album",
            subtitle = "${screen.album.artists
                ?.firstOrNull()
                ?.name ?: "Unknown"} · ${screen.album.releaseDate?.take(4) ?: ""}",
            onPlay = { mm.playPlaylist(screen.album.uri ?: return@drawDetailHeader) },
        )
        drawTrackListBody(nvg, palette, accentColor, mm, trackListState.get(), mouseX, mouseY, showArtist = false, showAlbum = false)
        drawSharedBottomControls(nvg, palette, accentColor, mm, mouseX, mouseY)
    }

    private fun drawDetailHeader(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mouseX: Int,
        mouseY: Int,
        imageUrl: String?,
        title: String,
        subtitle: String,
        isCircleArt: Boolean = false,
        onPlay: (() -> Unit)? = null,
    ) {
        nvg.drawRoundedRectVarying(
            getX().toFloat(),
            getY().toFloat(),
            getWidth().toFloat(),
            HEADER_H,
            12f,
            12f,
            0f,
            0f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 180),
        )
        val backHov = MouseUtils.isInside(mouseX, mouseY, getX() + 10f, getY() + 10f, 20f, 20f)
        nvg.drawText(
            Lucide.ARROW_LEFT,
            getX() + 10f,
            getY() + 10f,
            if (backHov) accentColor.getInterpolateColor() else palette.getFontColor(ColorType.NORMAL),
            16f,
            Fonts.LUCIDE,
        )

        val artSize = 80f
        val artX = getX() + 36f
        val artY = getY() + 15f
        val isValid = !imageUrl.isNullOrBlank() && imageUrl != AlbumArtCache.PLACEHOLDER_PATH && File(imageUrl!!).exists()
        if (isValid) {
            try {
                nvg.drawRoundedImage(File(imageUrl), artX, artY, artSize, artSize, 8f)
            } catch (_: Exception) {
                nvg.drawRoundedRect(artX, artY, artSize, artSize, 8f, Color(50, 50, 50))
            }
        } else {
            nvg.drawRoundedRect(artX, artY, artSize, artSize, 8f, Color(50, 50, 50))
            try {
                nvg.drawRoundedImage(PLACEHOLDER_IMAGE, artX, artY, artSize, artSize, 8f)
            } catch (_: Exception) {
            }
        }

        val textX = artX + artSize + 12f
        nvg.drawText(
            nvg.getLimitText(title, 14f, Fonts.SEMIBOLD, getWidth() - 20f),
            textX,
            artY + 14f,
            palette.getFontColor(ColorType.DARK),
            14f,
            Fonts.SEMIBOLD,
        )
        nvg.drawText(
            nvg.getLimitText(subtitle, 9f, Fonts.REGULAR, getWidth() - 20f),
            textX,
            artY + 34f,
            palette.getFontColor(ColorType.NORMAL),
            9f,
            Fonts.REGULAR,
        )

        if (onPlay != null) {
            val pby = artY + 50f
            val playHov = MouseUtils.isInside(mouseX, mouseY, textX, pby, 60f, 22f)
            nvg.drawRoundedRect(
                textX,
                pby,
                60f,
                22f,
                11f,
                if (playHov) accentColor.getInterpolateColor() else ColorUtils.applyAlpha(accentColor.getInterpolateColor(), 180),
            )
            nvg.drawText(Lucide.PLAY, textX + 8f, pby + 3f, Color.WHITE, 14f, Fonts.LUCIDE)
            nvg.drawText("Play", textX + 26f, pby + 6f, Color.WHITE, 9f, Fonts.MEDIUM)
        }
    }

    private fun drawTrackListBody(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mm: MusicManager,
        state: ContentState<TrackListContent>,
        mouseX: Int,
        mouseY: Int,
        showArtist: Boolean,
        showAlbum: Boolean,
    ) {
        when (state) {
            ContentState.Idle, is ContentState.Loading -> {
                drawLoadingSpinner(nvg, palette)
            }

            is ContentState.Error -> {
                drawErrorMessage(nvg, palette, state.message)
            }

            is ContentState.Ready -> {
                val listTop = getY() + HEADER_H + 4f
                val listH = getHeight() - HEADER_H - CONTROL_BAR_H - 4f
                val currentTrack = mm.getCurrentTrack()

                ModMenuClipCoordinator.withClip(
                    nvg = nvg,
                    x = getX().toFloat(),
                    y = listTop,
                    width = getWidth().toFloat(),
                    height = listH,
                ) {
                    nvg.save()
                    nvg.translate(0f, detailScroll.getValue())
                    state.data.tracks.forEachIndexed { idx, track ->
                        val rowY = listTop + idx * TRACK_ROW_SPACING
                        val isPlaying = currentTrack?.id == track.id
                        val hovered =
                            MouseUtils.isInside(
                                mouseX,
                                mouseY,
                                getX().toFloat(),
                                rowY + detailScroll.getValue(),
                                getWidth().toFloat(),
                                TRACK_ROW_H,
                            )
                        drawCompactTrackRow(
                            nvg,
                            palette,
                            accentColor,
                            track,
                            idx + 1,
                            rowY,
                            hovered,
                            isPlaying,
                            showArtist,
                            showAlbum,
                            mouseX,
                            mouseY,
                        )
                    }
                    nvg.restore()
                }

                detailScroll.maxScroll = max(0f, state.data.tracks.size * TRACK_ROW_SPACING - listH + 8f)
                nvg.drawVerticalGradientRect(
                    getX().toFloat(),
                    listTop,
                    getWidth().toFloat(),
                    10f,
                    palette.getBackgroundColor(ColorType.NORMAL),
                    noColor,
                )
                nvg.drawVerticalGradientRect(
                    getX().toFloat(),
                    listTop + listH - 10f,
                    getWidth().toFloat(),
                    10f,
                    noColor,
                    palette.getBackgroundColor(ColorType.NORMAL),
                )
            }
        }
    }

    private fun drawCompactTrackRow(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        track: Track,
        number: Int,
        rowY: Float,
        hovered: Boolean,
        isPlaying: Boolean,
        showArtist: Boolean,
        showAlbum: Boolean,
        mouseX: Int,
        mouseY: Int,
    ) {
        val rowX = getX().toFloat()
        val rowW = getWidth().toFloat()

        if (hovered) {
            nvg.drawRoundedRect(
                rowX + 8f,
                rowY + 1f,
                rowW - 16f,
                TRACK_ROW_H - 2f,
                6f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 120),
            )
        }

        if (isPlaying) {
            nvg.drawText(Lucide.VOLUME_2, rowX + 12f, rowY + 11f, accentColor.getInterpolateColor(), 14f, Fonts.LUCIDE)
        } else {
            nvg.drawCenteredText(
                "$number",
                rowX + 22f,
                rowY + 18f,
                if (hovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
                9f,
                Fonts.REGULAR,
            )
        }

        drawSmallArt(nvg, Shindo.getInstance().getMusicManager().getAlbumArtUrl(track), rowX + 34f, rowY + 5f, 28f, 4f)

        val titleColor = if (isPlaying) accentColor.getInterpolateColor() else palette.getFontColor(ColorType.DARK)
        val titleMaxW = rowW * 0.38f
        nvg.drawText(
            nvg.getLimitText(track.name ?: "Unknown", 10f, Fonts.MEDIUM, titleMaxW),
            rowX + 68f,
            rowY + (if (showArtist) 9f else 14f),
            titleColor,
            10f,
            Fonts.MEDIUM,
        )

        if (showArtist) {
            val artist = track.artists?.firstOrNull()?.name ?: ""
            val artistHov = hovered && MouseUtils.isInside(mouseX, mouseY, rowX + 68f, rowY + 21f, titleMaxW, 12f)
            nvg.drawText(
                nvg.getLimitText(artist, 8f, Fonts.REGULAR, titleMaxW),
                rowX + 68f,
                rowY + 21f,
                if (artistHov) accentColor.getInterpolateColor() else palette.getFontColor(ColorType.NORMAL),
                8f,
                Fonts.REGULAR,
            )
        }

        if (showAlbum && rowW > 280f) {
            val albumX = rowX + rowW * 0.52f
            val albumMaxW = rowW * 0.28f
            val albumHov = hovered && MouseUtils.isInside(mouseX, mouseY, albumX, rowY + 5f, albumMaxW, TRACK_ROW_H)
            nvg.drawText(
                nvg.getLimitText(track.album?.name ?: "", 9f, Fonts.REGULAR, albumMaxW),
                albumX,
                rowY + 14f,
                if (albumHov) accentColor.getInterpolateColor() else palette.getFontColor(ColorType.NORMAL),
                9f,
                Fonts.REGULAR,
            )
        }

        val dur = formatDuration(track.durationMs.toLong())
        nvg.drawText(
            dur,
            rowX + rowW - nvg.getTextWidth(dur, 9f, Fonts.REGULAR) - 28f,
            rowY + 14f,
            palette.getFontColor(ColorType.NORMAL),
            9f,
            Fonts.REGULAR,
        )

        if (hovered) {
            val qx = rowX + rowW - 22f
            val qHov = MouseUtils.isInside(mouseX, mouseY, qx, rowY + 11f, 16f, 16f)
            nvg.drawText(
                Lucide.PLUS_SQUARE,
                qx,
                rowY + 11f,
                if (qHov) accentColor.getInterpolateColor() else palette.getFontColor(ColorType.NORMAL),
                14f,
                Fonts.LUCIDE,
            )
        }
    }

    private fun drawTrackEntry(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        track: Track,
        x: Float,
        w: Float,
        offsetY: Float,
        mouseX: Int,
        mouseY: Int,
    ) {
        val absY = getY() + offsetY
        val hovered = MouseUtils.isInside(mouseX, mouseY, x, absY + libraryScroll.getValue(), w, ENTRY_ITEM_H)
        drawEntryShell(nvg, palette, x, absY, w)
        drawSmallArt(nvg, Shindo.getInstance().getMusicManager().getAlbumArtUrl(track), x + 5f, absY + 5f, 36f, 6f)
        nvg.drawText(
            nvg.getLimitText(track.name ?: "Unknown Track", 10f, Fonts.MEDIUM, w - 80f),
            x + 48f,
            absY + 9f,
            palette.getFontColor(ColorType.DARK),
            10f,
            Fonts.MEDIUM,
        )
        val artistHov = hovered && MouseUtils.isInside(mouseX, mouseY, x + 48f, absY + 21f + libraryScroll.getValue(), 120f, 12f)
        nvg.drawText(
            track.artists?.firstOrNull()?.name ?: "",
            x + 48f,
            absY + 21f,
            if (artistHov) accentColor.getInterpolateColor() else palette.getFontColor(ColorType.NORMAL),
            8f,
            Fonts.REGULAR,
        )
        val album = track.album?.name ?: ""
        if (album.isNotEmpty()) {
            val albumHov = hovered && MouseUtils.isInside(mouseX, mouseY, x + 48f, absY + 31f + libraryScroll.getValue(), 120f, 12f)
            nvg.drawText(
                nvg.getLimitText(album, 8f, Fonts.REGULAR, w - 80f),
                x + 48f,
                absY + 31f,
                if (albumHov) accentColor.getInterpolateColor() else palette.getFontColor(ColorType.NORMAL),
                8f,
                Fonts.REGULAR,
            )
        }
        val dur = formatDuration(track.durationMs.toLong())
        nvg.drawText(
            dur,
            x + w - nvg.getTextWidth(dur, 8f, Fonts.REGULAR) - 36f,
            absY + 19f,
            palette.getFontColor(ColorType.NORMAL),
            8f,
            Fonts.REGULAR,
        )
        nvg.drawText(
            Lucide.PLUS_SQUARE,
            x + w - 24f,
            absY + 15f,
            if (hovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
            16f,
            Fonts.LUCIDE,
        )
    }

    private fun drawPlaylistEntry(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        playlist: PlaylistSimplified,
        x: Float,
        w: Float,
        offsetY: Float,
        mouseX: Int,
        mouseY: Int,
    ) {
        val absY = getY() + offsetY
        val hovered = MouseUtils.isInside(mouseX, mouseY, x, absY + libraryScroll.getValue(), w, ENTRY_ITEM_H)
        drawEntryShell(nvg, palette, x, absY, w)
        drawSmallArt(nvg, Shindo.getInstance().getMusicManager().getPlaylistImageUrl(playlist), x + 5f, absY + 5f, 36f, 6f)
        nvg.drawText(
            nvg.getLimitText(playlist.name ?: "Untitled", 10f, Fonts.MEDIUM, w - 60f),
            x + 48f,
            absY + 9f,
            palette.getFontColor(ColorType.DARK),
            10f,
            Fonts.MEDIUM,
        )
        nvg.drawText(playlist.owner?.displayName ?: "", x + 48f, absY + 23f, palette.getFontColor(ColorType.NORMAL), 8f, Fonts.REGULAR)
        nvg.drawText(
            Lucide.PLAY,
            x + w - 24f,
            absY + 15f,
            if (hovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
            16f,
            Fonts.LUCIDE,
        )
    }

    private fun drawEntryShell(
        nvg: NanoVGManager,
        palette: ColorPalette,
        x: Float,
        y: Float,
        w: Float,
    ) {
        nvg.drawShadow(x, y, w, ENTRY_ITEM_H, 8f, 7)
        nvg.drawRoundedRect(x, y, w, ENTRY_ITEM_H, 8f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 220))
        nvg.drawOutlineRoundedRect(x, y, w, ENTRY_ITEM_H, 8f, 1f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210))
    }

    private fun drawLyricsView(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mm: MusicManager,
        mouseX: Int,
        mouseY: Int,
    ) {
        nvg.drawRoundedRect(
            getX().toFloat(),
            getY().toFloat(),
            getWidth().toFloat(),
            getHeight() - CONTROL_BAR_H,
            0f,
            palette.getBackgroundColor(ColorType.NORMAL),
        )
        val backHov = MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + 15f, 16f, 16f)
        nvg.drawText(
            Lucide.ARROW_LEFT,
            getX() + 15f,
            getY() + 15f,
            if (backHov) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
            16f,
            Fonts.LUCIDE,
        )
        if (mm.getCurrentTrack() != null) {
            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    getX().toFloat(),
                    getY().toFloat(),
                    getWidth().toFloat(),
                    getHeight() - CONTROL_BAR_H,
                )
            ) {
                lyricsScroll.onScroll()
            }
            lyricsScroll.onAnimation()
            drawScrollableLyrics(nvg, palette, accentColor, mm, mouseX, mouseY, 0f, trackPosition)
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
        drawSharedBottomControls(nvg, palette, accentColor, mm, mouseX, mouseY)
    }

    private fun drawScrollableLyrics(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mm: MusicManager,
        mouseX: Int,
        mouseY: Int,
        startY: Float,
        currentPosition: Long,
    ) {
        val lyricsManager = mm.getLyricsManager()
        val lyrics = lyricsManager.getCurrentLyrics()
        if (lyrics == null || lyrics.isError() || lyrics.lines.isEmpty()) {
            nvg.drawCenteredText(
                "No lyrics available",
                getX() + getWidth() / 2f,
                getY() + getHeight() / 2.7f,
                palette.getFontColor(ColorType.NORMAL),
                14f,
                Fonts.MEDIUM,
            )
            return
        }
        lyricsManager.updateCurrentLineIndex(currentPosition)
        val currentLineIndex = lyricsManager.getCurrentLineIndex()
        val lyricsAreaH = getHeight() - startY - CONTROL_BAR_H
        val maxTextW = getWidth() - 60f
        currentHighlightedLyricIndex = -1
        var totalH = 0f
        val lineHeights = IntArray(lyrics.lines.size)
        val wrappedLines = arrayOfNulls<Array<String>>(lyrics.lines.size)
        for (i in lyrics.lines.indices) {
            val text = extractLyricsText(lyrics.lines[i])
            if (text.isEmpty()) {
                lineHeights[i] = 30
                wrappedLines[i] = emptyArray()
                continue
            }
            val fs = if (i == currentLineIndex) 14f else 12f
            val wrapped = wrapText(nvg, text, fs, Fonts.MEDIUM, maxTextW)
            wrappedLines[i] = wrapped
            lineHeights[i] = if (wrapped.size <= 1) 30 else (fs * wrapped.size * 1.0f + 10).toInt()
            totalH += lineHeights[i]
        }
        ModMenuClipCoordinator.withClip(nvg = nvg, x = getX() + 15f, y = getY() + startY, width = getWidth() - 30f, height = lyricsAreaH) {
            var cy = getY() + startY + lyricsScroll.getValue()
            val vTop = getY() + startY
            val vBot = vTop + lyricsAreaH
            for (i in lyrics.lines.indices) {
                val lh = lineHeights[i].toFloat()
                if (cy + lh < vTop || cy > vBot) {
                    cy += lh
                    continue
                }
                val isCurrent = i == currentLineIndex
                val isHov = MouseUtils.isInside(mouseX, mouseY, getX() + 20f, cy, getWidth() - 40f, lh)
                if (isHov) currentHighlightedLyricIndex = i
                val (col, fs) =
                    when {
                        isCurrent -> {
                            accentColor.getInterpolateColor() to 14f
                        }

                        isHov -> {
                            palette.getFontColor(ColorType.DARK) to
                                12f
                        }

                        else -> {
                            palette.getFontColor(ColorType.NORMAL) to 12f
                        }
                    }
                if (isCurrent) {
                    nvg.drawRoundedRect(
                        getX() + 20f,
                        cy,
                        getWidth() - 40f,
                        lh,
                        4f,
                        Color(accentColor.getColor1().red, accentColor.getColor1().green, accentColor.getColor1().blue, 30),
                    )
                }
                var wo = 0f
                for (line in wrappedLines[i] ?: emptyArray()) {
                    val tx = getX() + getWidth() / 2f - nvg.getTextWidth(line, fs, Fonts.MEDIUM) / 2f
                    val ty = cy + wo + fs / 2f
                    if (isCurrent) {
                        nvg.drawTextGlowing(line, tx, ty, col, 8f, fs, Fonts.MEDIUM)
                    } else {
                        nvg.drawText(line, tx, ty, col, fs, Fonts.MEDIUM)
                    }
                    wo += fs
                }
                cy += lh
            }
        }
        lyricsScroll.maxScroll = max(0f, totalH - lyricsAreaH + 20f)
        if (lyricsScroll.getValue() <
            0
        ) {
            nvg.drawVerticalGradientRect(
                getX() + 15f,
                getY() + startY,
                getWidth() - 30f,
                12f,
                palette.getBackgroundColor(ColorType.NORMAL),
                noColor,
            )
        }
        if (-lyricsScroll.getValue() <
            lyricsScroll.maxScroll
        ) {
            nvg.drawVerticalGradientRect(
                getX() + 15f,
                getY() + startY + lyricsAreaH - 12f,
                getWidth() - 30f,
                12f,
                noColor,
                palette.getBackgroundColor(ColorType.NORMAL),
            )
        }
    }

    private fun drawSharedBottomControls(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mm: MusicManager,
        mouseX: Int,
        mouseY: Int,
    ) {
        drawControlBar(nvg, palette, mm)
        drawPlaybackControls(nvg, palette, mm)
        drawVolumeSlider(nvg, palette, mouseX, mouseY, 0f)
        drawProgressBar(nvg, accentColor, palette)
    }

    private fun drawControlBar(
        nvg: NanoVGManager,
        palette: ColorPalette,
        mm: MusicManager,
    ) {
        nvg.drawRoundedRectVarying(
            getX().toFloat(),
            getY() + getHeight() - CONTROL_BAR_H,
            getWidth().toFloat(),
            CONTROL_BAR_H,
            0f,
            0f,
            0f,
            12f,
            palette.getBackgroundColor(ColorType.DARK),
        )
        val track = mm.getCurrentTrack()
        val artY = getY() + getHeight() - 43f
        if (track != null) {
            val url = mm.getAlbumArtUrl(track)
            val valid = !url.isNullOrBlank() && url != AlbumArtCache.PLACEHOLDER_PATH && File(url!!).exists()
            if (valid) {
                try {
                    nvg.drawRoundedImage(File(url!!), getX() + 4f, artY, 36f, 36f, 6f)
                } catch (
                    _: Exception,
                ) {
                    drawBarPlaceholder(nvg, artY)
                }
            } else {
                drawBarPlaceholder(nvg, artY)
            }
            nvg.drawText(
                nvg.getLimitText(track.name ?: "Unknown", 9f, Fonts.MEDIUM, 100f),
                getX() + 45f,
                getY() + getHeight() - 39f,
                palette.getFontColor(ColorType.DARK),
                9f,
                Fonts.MEDIUM,
            )
            nvg.drawText(
                nvg.getLimitText(track.artists?.firstOrNull()?.name ?: "", 9f, Fonts.REGULAR, 100f),
                getX() + 45f,
                getY() + getHeight() - 27f,
                palette.getFontColor(ColorType.NORMAL),
                9f,
                Fonts.REGULAR,
            )
        } else {
            drawBarPlaceholder(nvg, artY)
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

    private fun drawBarPlaceholder(
        nvg: NanoVGManager,
        artY: Float,
    ) {
        nvg.drawRoundedRect(getX() + 4f, artY, 36f, 36f, 6f, Color(50, 50, 50))
        try {
            nvg.drawRoundedImage(PLACEHOLDER_IMAGE, getX() + 4f, artY, 36f, 36f, 6f)
        } catch (_: Exception) {
        }
    }

    private fun drawPlaybackControls(
        nvg: NanoVGManager,
        palette: ColorPalette,
        mm: MusicManager,
    ) {
        val cx = getX() + getWidth() / 2f
        val cy = getY() + getHeight() - 32f
        val col = palette.getFontColor(ColorType.NORMAL)
        nvg.drawText(Lucide.REWIND, cx - 32f, cy, col, 16f, Fonts.LUCIDE)
        nvg.drawText(if (mm.isPlaying()) Lucide.PAUSE else Lucide.PLAY, cx - 8f, cy, col, 16f, Fonts.LUCIDE)
        nvg.drawText(Lucide.FAST_FORWARD, cx + 16f, cy, col, 16f, Fonts.LUCIDE)
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
        val vol = (volumeSlider.getSetting().getValueFloat() * 100).toInt()
        nvg.drawText(
            when {
                vol == 0 -> Lucide.VOLUME_X
                vol > 80 -> Lucide.VOLUME_2
                vol > 40 -> Lucide.VOLUME_1
                else -> Lucide.VOLUME
            },
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

    private fun drawLoadingSpinner(
        nvg: NanoVGManager,
        palette: ColorPalette,
    ) {
        nvg.drawCenteredText(
            "Loading...",
            getX() + getWidth() / 2f,
            getY() + (getHeight() - CONTROL_BAR_H) / 2f,
            palette.getFontColor(ColorType.NORMAL),
            11f,
            Fonts.MEDIUM,
        )
    }

    private fun drawErrorMessage(
        nvg: NanoVGManager,
        palette: ColorPalette,
        message: String,
    ) {
        val cy = getY() + (getHeight() - CONTROL_BAR_H) / 2f
        nvg.drawCenteredText("Failed to load", getX() + getWidth() / 2f, cy - 8f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
        nvg.drawCenteredText(message.take(60), getX() + getWidth() / 2f, cy + 6f, palette.getFontColor(ColorType.NORMAL), 9f, Fonts.REGULAR)
    }

    private fun drawSmallArt(
        nvg: NanoVGManager,
        imageUrl: String?,
        x: Float,
        y: Float,
        size: Float,
        radius: Float,
    ) {
        val isValid = !imageUrl.isNullOrBlank() && imageUrl != AlbumArtCache.PLACEHOLDER_PATH && File(imageUrl!!).exists()
        if (isValid) {
            try {
                nvg.drawRoundedImage(File(imageUrl!!), x, y, size, size, radius)
                return
            } catch (_: Exception) {
            }
        }
        nvg.drawRoundedRect(x, y, size, size, radius, Color(50, 50, 50))
        try {
            nvg.drawRoundedImage(PLACEHOLDER_IMAGE, x, y, size, size, radius)
        } catch (_: Exception) {
        }
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (!MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight().toFloat())) return
        if (showConnectButton) {
            if (mouseButton == 0 &&
                MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() / 2f - 75f, getY() + getHeight() / 2f - 20f, 150f, 40f)
            ) {
                openConfirmDialog(Shindo.getInstance().getMusicManager().getAuthorizationCodeUri())
                showConnectButton = false
            }
            return
        }
        if (mouseButton == 0 && mouseY >= getY() + getHeight() - CONTROL_BAR_H) {
            handleControlBarClick(mouseX, mouseY)
            return
        }
        when (val screen = navigator.current) {
            is SpotifyScreen.Library -> handleLibraryClick(mouseX, mouseY, mouseButton)
            is SpotifyScreen.Lyrics -> handleLyricsClick(mouseX, mouseY, mouseButton)
            is SpotifyScreen.PlaylistDetail -> handleDetailClick(mouseX, mouseY, mouseButton, screen)
            is SpotifyScreen.ArtistDetail -> handleArtistClick(mouseX, mouseY, mouseButton)
            is SpotifyScreen.AlbumDetail -> handleAlbumClick(mouseX, mouseY, mouseButton, screen)
        }
    }

    private fun handleLibraryClick(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton != 0) return
        val bx = getX() + getWidth() - 116f
        val by = getY() + getHeight() - 26f
        if (MouseUtils.isInside(mouseX, mouseY, bx, by, 16f, 16f)) {
            navigator.push(SpotifyScreen.Lyrics)
            lyricsScroll.resetAll()
            val mm = Shindo.getInstance().getMusicManager()
            mm.getCurrentTrack()?.let { mm.getLyricsManager().fetchLyrics(it) }
            return
        }
        if (MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), SIDEBAR_WIDTH, getHeight() - CONTROL_BAR_H)) {
            val playlists = userPlaylists ?: return
            val sx = getX() + 10f
            val sw = SIDEBAR_WIDTH - 20f
            var rowY = getY() + 38f + sidebarScroll.getValue() + 4f
            for (playlist in playlists) {
                if (MouseUtils.isInside(mouseX, mouseY, sx, rowY, sw, SIDEBAR_ROW_H)) {
                    navigateToPlaylist(playlist)
                    return
                }
                rowY += SIDEBAR_ROW_SPACING
            }
            return
        }
        val snapshot = searchSnapshot.get() ?: return
        val mainX = getX() + SIDEBAR_WIDTH
        val mainW = getWidth() - SIDEBAR_WIDTH
        if (!MouseUtils.isInside(mouseX, mouseY, mainX, getY().toFloat(), mainW, getHeight() - CONTROL_BAR_H)) return
        var offsetY = 13f + libraryScroll.getValue()
        snapshot.tracks.forEach { track ->
            val entryY = getY() + offsetY - libraryScroll.getValue()
            if (MouseUtils.isInside(mouseX, mouseY, mainX + 8f, getY() + offsetY, mainW - 16f, ENTRY_ITEM_H)) {
                if (MouseUtils.isInside(mouseX, mouseY, mainX + mainW - 32f, getY() + offsetY + 15f, 16f, 16f)) {
                    addToQueue(track)
                    return
                }
                if (MouseUtils.isInside(mouseX, mouseY, mainX + 48f, entryY + 21f, 120f, 12f)) {
                    track.artists?.firstOrNull()?.let { navigateToArtist(it) }
                    return
                }
                if (MouseUtils.isInside(mouseX, mouseY, mainX + 48f, entryY + 31f, 120f, 12f)) {
                    track.album?.id?.let { navigateToAlbum(track.album, it) }
                    return
                }
                Shindo.getInstance().getMusicManager().play(track.uri)
                return
            }
            offsetY += ENTRY_HEIGHT
        }
        snapshot.playlists.forEach { playlist ->
            if (MouseUtils.isInside(mouseX, mouseY, mainX + 8f, getY() + offsetY, mainW - 16f, ENTRY_ITEM_H)) {
                playlist.uri?.let { uri ->
                    if (MouseUtils.isInside(
                            mouseX,
                            mouseY,
                            mainX + mainW - 32f,
                            getY() + offsetY + 15f,
                            16f,
                            16f,
                        )
                    ) {
                        navigateToPlaylist(playlist)
                    } else {
                        Shindo.getInstance().getMusicManager().playPlaylist(uri)
                    }
                }
                return
            }
            offsetY += ENTRY_HEIGHT
        }
    }

    private fun handleDetailClick(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
        screen: SpotifyScreen.PlaylistDetail,
    ) {
        if (mouseButton != 0) return
        if (handleBackClick(mouseX, mouseY)) return
        if (handlePlayHeaderClick(mouseX, mouseY)) {
            Shindo.getInstance().getMusicManager().playPlaylist(screen.playlist.uri ?: return)
            return
        }
        handleTrackListClick(mouseX, mouseY)
    }

    private fun handleArtistClick(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton != 0) return
        if (handleBackClick(mouseX, mouseY)) return
        val state = artistState.get()
        if (state is ContentState.Ready) {
            val fakeState = ContentState.Ready(TrackListContent(tracks = state.data.topTracks, totalCount = state.data.topTracks.size))
            handleTrackListClickWithState(mouseX, mouseY, fakeState)
        }
    }

    private fun handleAlbumClick(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
        screen: SpotifyScreen.AlbumDetail,
    ) {
        if (mouseButton != 0) return
        if (handleBackClick(mouseX, mouseY)) return
        if (handlePlayHeaderClick(mouseX, mouseY)) {
            Shindo.getInstance().getMusicManager().playPlaylist(screen.album.uri ?: return)
            return
        }
        handleTrackListClick(mouseX, mouseY)
    }

    private fun handleBackClick(
        mouseX: Int,
        mouseY: Int,
    ): Boolean {
        if (MouseUtils.isInside(mouseX, mouseY, getX() + 10f, getY() + 10f, 20f, 20f)) {
            navigator.pop()
            detailScroll.resetAll()
            trackListState.set(ContentState.Idle)
            artistState.set(ContentState.Idle)
            return true
        }
        return false
    }

    private fun handlePlayHeaderClick(
        mouseX: Int,
        mouseY: Int,
    ): Boolean = MouseUtils.isInside(mouseX, mouseY, getX() + 36f + 80f + 12f, getY() + 15f + 50f, 60f, 22f)

    private fun handleTrackListClick(
        mouseX: Int,
        mouseY: Int,
    ) = handleTrackListClickWithState(mouseX, mouseY, trackListState.get())

    private fun handleTrackListClickWithState(
        mouseX: Int,
        mouseY: Int,
        state: ContentState<TrackListContent>,
    ) {
        if (state !is ContentState.Ready) return
        val listTop = getY() + HEADER_H + 4f
        state.data.tracks.forEachIndexed { idx, track ->
            val rowY = listTop + idx * TRACK_ROW_SPACING + detailScroll.getValue()
            if (MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), rowY, getWidth().toFloat(), TRACK_ROW_H)) {
                if (MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() - 22f, rowY + 11f, 16f, 16f)) {
                    addToQueue(track)
                    return
                }
                if (MouseUtils.isInside(mouseX, mouseY, getX() + 68f, rowY + 21f, getWidth() * 0.38f, 12f)) {
                    track.artists?.firstOrNull()?.let { navigateToArtist(it) }
                    return
                }
                val albumX = getX() + getWidth() * 0.52f
                if (MouseUtils.isInside(mouseX, mouseY, albumX, rowY + 5f, getWidth() * 0.28f, TRACK_ROW_H)) {
                    track.album?.id?.let { navigateToAlbum(track.album, it) }
                    return
                }
                Shindo.getInstance().getMusicManager().play(track.uri)
                return
            }
        }
    }

    private fun handleLyricsClick(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton != 0) return
        if (MouseUtils.isInside(mouseX, mouseY, getX() + 15f, getY() + 15f, 16f, 16f)) {
            navigator.pop()
            return
        }
        if (currentHighlightedLyricIndex >= 0) {
            val mm = Shindo.getInstance().getMusicManager()
            val lyrics = mm.getLyricsManager().getCurrentLyrics()
            if (lyrics != null && !lyrics.isError() && currentHighlightedLyricIndex < lyrics.lines.size) {
                mm.seekToPosition(lyrics.lines[currentHighlightedLyricIndex].startTime)
            }
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

    override fun mouseReleased(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (!showConnectButton) {
            volumeSlider.mouseReleased(mouseX, mouseY, mouseButton)
            updateVolume()
        }
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        if (showConnectButton) return
        val mm = Shindo.getInstance().getMusicManager()
        val searchFocused = parentRef.get()?.getSearchBox()?.isFocused() == true
        if (keyCode == Keyboard.KEY_ESCAPE && navigator.canGoBack) {
            navigator.pop()
            detailScroll.resetAll()
            return
        }
        if (keyCode == Keyboard.KEY_SPACE && !searchFocused) {
            if (mm.isPlaying()) mm.pause() else mm.resume()
        }
        when (keyCode) {
            Keyboard.KEY_UP -> adjustVolume(mm, +5)
            Keyboard.KEY_DOWN -> adjustVolume(mm, -5)
            Keyboard.KEY_RIGHT -> mm.seekToPosition(min(trackPosition + 10_000, trackDuration))
            Keyboard.KEY_LEFT -> mm.seekToPosition(max(trackPosition - 10_000, 0))
        }
        when (navigator.current) {
            is SpotifyScreen.Lyrics -> lyricsScroll.onKey(keyCode)
            is SpotifyScreen.PlaylistDetail, is SpotifyScreen.ArtistDetail, is SpotifyScreen.AlbumDetail -> detailScroll.onKey(keyCode)
            else -> libraryScroll.onKey(keyCode)
        }
    }

    private fun navigateToPlaylist(playlist: PlaylistSimplified) {
        val id = playlist.id ?: return
        trackListState.set(ContentState.Loading)
        detailScroll.resetAll()
        navigator.push(SpotifyScreen.PlaylistDetail(playlist, id))
        Shindo
            .getInstance()
            .getMusicManager()
            .getPlaylistTracks(id)
            .thenAccept { trackListState.set(ContentState.Ready(it)) }
            .exceptionally { ex ->
                trackListState.set(ContentState.Error(ex.message ?: "Unknown error"))
                null
            }
    }

    private fun navigateToArtist(artist: ArtistSimplified) {
        val id = artist.id ?: return
        artistState.set(ContentState.Loading)
        detailScroll.resetAll()
        navigator.push(SpotifyScreen.ArtistDetail(artist))
        Shindo
            .getInstance()
            .getMusicManager()
            .getArtistContent(id)
            .thenAccept { artistState.set(ContentState.Ready(it)) }
            .exceptionally { ex ->
                artistState.set(ContentState.Error(ex.message ?: "Unknown error"))
                null
            }
    }

    private fun navigateToAlbum(
        album: com.wrapper.spotify.model_objects.specification.AlbumSimplified,
        albumId: String,
    ) {
        trackListState.set(ContentState.Loading)
        detailScroll.resetAll()
        navigator.push(SpotifyScreen.AlbumDetail(album, albumId))
        Shindo
            .getInstance()
            .getMusicManager()
            .getAlbumTracks(albumId)
            .thenAccept { trackListState.set(ContentState.Ready(it)) }
            .exceptionally { ex ->
                trackListState.set(ContentState.Error(ex.message ?: "Unknown error"))
                null
            }
    }

    override fun onTrackInfoUpdated(
        position: Long,
        duration: Long,
    ) {
        trackPosition = position
        trackDuration = duration
        val mm = Shindo.getInstance().getMusicManager()
        val track = mm.getCurrentTrack()
        if (track != null) {
            if (track.id != currentTrackId) {
                currentTrackId = track.id
                mm.getAlbumArtUrl(track)
                if (navigator.current is SpotifyScreen.Lyrics) {
                    mm.getLyricsManager().fetchLyrics(track)
                    lyricsScroll.resetAll()
                }
            }
        } else {
            currentTrackId = null
        }
    }

    private fun checkAndUpdateSearch() {
        val query = parentRef.get()?.getSearchBox()?.getText() ?: return
        if (query != lastSearchQuery) {
            scheduleSearch(query)
            lastSearchQuery = query
        }
    }

    private fun scheduleSearch(query: String) {
        if (query.isEmpty()) {
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
                    tracks.take(5).forEach { mm.getAlbumArtUrl(it) }
                    playlists.take(5).forEach { mm.getPlaylistImageUrl(it) }
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

    private fun updateLibraryScroll(snapshot: SearchSnapshot?) {
        val count = (snapshot?.tracks?.size ?: 0) + (snapshot?.playlists?.size ?: 0)
        libraryScroll.maxScroll = max(0f, count * ENTRY_HEIGHT - (getHeight() - CONTROL_BAR_H) + 20f)
    }

    private fun isEntryVisible(
        offsetY: Float,
        clipTop: Float,
        clipH: Float,
    ): Boolean {
        val scrolled = -libraryScroll.getValue()
        return offsetY + ENTRY_ITEM_H >= scrolled && offsetY <= scrolled + clipH
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

    private fun adjustVolume(
        mm: MusicManager,
        delta: Int,
    ) {
        val v = ((volumeSlider.getSetting().getValueFloat() * 100).toInt() + delta).coerceIn(0, 100)
        volumeSlider.getSetting().setValue(v / 100.0)
        mm.setVolume(v)
        lastVolumeChangeTime = System.currentTimeMillis()
    }

    private fun updateVolume() {
        val now = System.currentTimeMillis()
        if (now - lastVolumeChangeTime > VOLUME_CHANGE_DELAY_MS) {
            lastVolumeChangeTime = now
            Shindo.getInstance().getMusicManager().setVolume((volumeSlider.getSetting().getValueFloat() * 100).toInt())
        }
    }

    private fun openConfirmDialog(uri: String) {
        val mc = Minecraft.getMinecraft()
        val gui =
            GuiConfirmOpenLink({ result, _ ->
                if (result) {
                    try {
                        BrowserUtils.tryOpenBrowser(uri)
                    } catch (_: Exception) {
                    }
                }
                mc.displayGuiScreen(parentRef.get())
            }, uri, 0, true)
        gui.disableSecurityWarning()
        mc.displayGuiScreen(gui)
    }

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
        var cur = StringBuilder()
        for (word in text.split(" ")) {
            val test = if (cur.isNotEmpty()) "$cur $word" else word
            if (nvg.getTextWidth(test, fontSize, font) <= maxWidth) {
                if (cur.isNotEmpty()) cur.append(" ")
                cur.append(word)
            } else {
                if (cur.isNotEmpty()) {
                    lines.add(cur.toString())
                    cur = StringBuilder()
                }
                if (nvg.getTextWidth(word, fontSize, font) > maxWidth) {
                    var p = StringBuilder()
                    for (c in word) {
                        if (nvg.getTextWidth("$p$c", fontSize, font) <=
                            maxWidth
                        ) {
                            p.append(c)
                        } else {
                            lines.add(p.toString())
                            p = StringBuilder().append(c)
                        }
                    }
                    if (p.isNotEmpty()) cur = p
                } else {
                    cur.append(word)
                }
            }
        }
        if (cur.isNotEmpty()) lines.add(cur.toString())
        return lines.toTypedArray()
    }

    private fun extractLyricsText(line: LyricsLine): String =
        when {
            !line.words.isNullOrEmpty() -> line.words
            !line.romanizedWords.isNullOrEmpty() -> line.romanizedWords!!
            else -> ""
        }

    private fun formatDuration(ms: Long): String {
        val s = ms / 1000L
        return "%d:%02d".format(s / 60, s % 60)
    }

    private fun formatFollowers(count: Long): String =
        when {
            count >= 1_000_000 -> "%.1fM".format(count / 1_000_000f)
            count >= 1_000 -> "%.1fK".format(count / 1_000f)
            else -> count.toString()
        }

    private companion object {
        private const val VOLUME_CHANGE_DELAY_MS = 500L
        private const val SEARCH_DEBOUNCE_DELAY_MS = 800L
        private val PLACEHOLDER_IMAGE = ResourceLocation("shindo/music.png")
    }
}
