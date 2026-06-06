package com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.content
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.nav.SpotifyNavigator
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.nav.SpotifyScreen
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.state.ContentState
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.player.SpotifyPlayerBar

import com.shindoclient.spotify.data.AlbumSimplified
import com.shindoclient.spotify.data.ArtistSimplified
import com.shindoclient.spotify.data.PlaylistSimplified
import com.shindoclient.spotify.data.Track
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.gui.modmenu.v2.category.section.CategorySectionCursor
import com.shindoclient.shindo.gui.modmenu.v2.category.section.CategorySectionRenderer
import com.shindoclient.shindo.gui.modmenu.v2.category.section.CategorySectionSpec
import com.shindoclient.shindo.gui.modmenu.v2.render.ModMenuClipCoordinator
import com.shindoclient.shindo.management.color.AccentColor
import com.shindoclient.shindo.management.color.palette.ColorPalette
import com.shindoclient.shindo.management.color.palette.ColorType
import com.shindoclient.shindo.management.music.MusicManager
import com.shindoclient.shindo.management.music.cache.AlbumArtCache
import com.shindoclient.shindo.management.music.data.ArtistContent
import com.shindoclient.shindo.management.music.data.SearchSnapshot
import com.shindoclient.shindo.management.music.data.TrackListContent
import com.shindoclient.shindo.management.music.model.LyricsLine
import com.shindoclient.shindo.management.nanovg.NanoVGManager
import com.shindoclient.shindo.management.nanovg.font.Font
import com.shindoclient.shindo.management.nanovg.font.Fonts
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.utils.ColorUtils
import com.shindoclient.shindo.utils.mouse.MouseUtils
import com.shindoclient.shindo.utils.mouse.Scroll
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

private const val SIDEBAR_WIDTH = 140f
private const val ENTRY_HEIGHT = 56f
private const val ENTRY_ITEM_H = 46f
private const val CONTROL_BAR_H = 46f
private const val TRACK_ROW_H = 38f
private const val TRACK_ROW_SPACING = 42f
private const val HEADER_H = 110f
private val PLACEHOLDER_IMAGE = ResourceLocation("shindo/music.png")

class SpotifyContentRenderer(
    private val getX: () -> Float,
    private val getY: () -> Float,
    private val getWidth: () -> Float,
    private val getHeight: () -> Float,
    private val navigator: SpotifyNavigator,
    private val trackListState: AtomicReference<ContentState<TrackListContent>>,
    private val artistState: AtomicReference<ContentState<ArtistContent>>,
    private val libraryScroll: Scroll,
    private val detailScroll: Scroll,
    private val lyricsScroll: Scroll,
    private val playerBar: SpotifyPlayerBar,
    private val getSearchSnapshot: () -> SearchSnapshot?,
    private val onUpdateLibraryScroll: (SearchSnapshot?) -> Unit,
) {
    private val noColor = Color(0, 0, 0, 0)

    @Volatile
    var currentHighlightedLyricIndex = -1

    fun drawLibraryLayout(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mm: MusicManager,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        drawSidebar: () -> Unit,
    ) {
        val mainX = getX() + SIDEBAR_WIDTH
        val mainW = getWidth() - SIDEBAR_WIDTH

        drawSidebar()

        libraryScroll.onScroll()
        libraryScroll.onAnimation()

        val snapshot = getSearchSnapshot()
        drawMainArea(nvg, palette, accentColor, mm, snapshot, mainX, mainW, mouseX, mouseY)

        playerBar.drawBottomControls(nvg, palette, accentColor, mm, mouseX, mouseY, partialTicks, showLyricsButton = true)

        nvg.drawVerticalGradientRect(
            mainX,
            getY().toFloat(),
            mainW,
            12f,
            palette.getBackgroundColor(ColorType.NORMAL),
            noColor,
        )
        nvg.drawVerticalGradientRect(
            mainX,
            getY() + getHeight() - CONTROL_BAR_H - 12f,
            mainW,
            12f,
            noColor,
            palette.getBackgroundColor(ColorType.NORMAL),
        )

        onUpdateLibraryScroll(snapshot)
    }

    fun drawMainArea(
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
                val cursor = CategorySectionCursor(startY = 13f)
                val sectionX = mainX + 8f

                if (snapshot.tracks.isNotEmpty()) {
                    val headerEnd = CategorySectionRenderer.drawHeader(
                        nvg, palette, sectionX, cursor.y,
                        CategorySectionSpec("Tracks", "${snapshot.tracks.size} results"),
                        cursor.style,
                    )
                    cursor.y = headerEnd
                    snapshot.tracks.forEach { track ->
                        drawTrackEntry(nvg, palette, accentColor, track, sectionX, mainW - 16f, cursor.y, mouseX, mouseY)
                        cursor.moveBy(ENTRY_HEIGHT)
                    }
                }

                if (snapshot.playlists.isNotEmpty()) {
                    cursor.nextSection()
                    val headerEnd = CategorySectionRenderer.drawHeader(
                        nvg, palette, sectionX, cursor.y,
                        CategorySectionSpec("Playlists", "${snapshot.playlists.size} results"),
                        cursor.style,
                    )
                    cursor.y = headerEnd
                    snapshot.playlists.forEach { playlist ->
                        drawPlaylistEntry(nvg, palette, accentColor, playlist, sectionX, mainW - 16f, cursor.y, mouseX, mouseY)
                        cursor.moveBy(ENTRY_HEIGHT)
                    }
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

    fun drawPlaylistDetailScreen(
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
            subtitle = "by ${screen.playlist.owner?.displayName ?: "Unknown"} · ${screen.playlist.tracksTotal} tracks",
            onPlay = { screen.playlist.uri?.let { mm.playPlaylist(it) } },
        )
        drawTrackListBody(nvg, palette, accentColor, mm, trackListState.get(), mouseX, mouseY, showArtist = true, showAlbum = true)
        playerBar.drawSharedBottomControls(nvg, palette, accentColor, mm, mouseX, mouseY)
    }

    fun drawArtistDetailScreen(
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
                playerBar.drawLoadingSpinner(nvg, palette)
            }

            is ContentState.Error -> {
                playerBar.drawErrorMessage(nvg, palette, state.message)
            }

            is ContentState.Ready -> {
                val wrapped = ContentState.Ready(TrackListContent(tracks = state.data.topTracks, totalCount = state.data.topTracks.size))
                drawTrackListBody(nvg, palette, accentColor, mm, wrapped, mouseX, mouseY, showArtist = false, showAlbum = true)
            }
        }
        playerBar.drawSharedBottomControls(nvg, palette, accentColor, mm, mouseX, mouseY)
    }

    fun drawAlbumDetailScreen(
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
            onPlay = { screen.album.uri?.let { mm.playPlaylist(it) } },
        )
        drawTrackListBody(nvg, palette, accentColor, mm, trackListState.get(), mouseX, mouseY, showArtist = false, showAlbum = false)
        playerBar.drawSharedBottomControls(nvg, palette, accentColor, mm, mouseX, mouseY)
    }

    fun drawDetailHeader(
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

    fun drawTrackListBody(
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
                playerBar.drawLoadingSpinner(nvg, palette)
            }

            is ContentState.Error -> {
                playerBar.drawErrorMessage(nvg, palette, state.message)
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

    fun drawCompactTrackRow(
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

    fun drawTrackEntry(
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

    fun drawPlaylistEntry(
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

        val text: String = if (playlist.name.isNullOrEmpty()) "Untitled" else playlist.name
        drawEntryShell(nvg, palette, x, absY, w)
        drawSmallArt(nvg, Shindo.getInstance().getMusicManager().getPlaylistImageUrl(playlist), x + 5f, absY + 5f, 36f, 6f)
        nvg.drawText(
            nvg.getLimitText(text, 10f, Fonts.MEDIUM, w - 60f),
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

    fun drawLyricsView(
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
            drawScrollableLyrics(nvg, palette, accentColor, mm, mouseX, mouseY, 0f, mm.getTrackPosition())
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
        playerBar.drawSharedBottomControls(nvg, palette, accentColor, mm, mouseX, mouseY)
    }

    fun drawScrollableLyrics(
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
        ModMenuClipCoordinator.withClip(
            nvg = nvg,
            x = getX() + 15f,
            y = getY() + startY,
            width = getWidth() - 30f,
            height = lyricsAreaH,
        ) {
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
                            palette.getFontColor(ColorType.DARK) to 12f
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
        if (-lyricsScroll.getValue() < lyricsScroll.maxScroll) {
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

    private fun drawSmallArt(
        nvg: NanoVGManager,
        imageUrl: String?,
        x: Float,
        y: Float,
        size: Float,
        radius: Float,
    ) {
        val isValid = !imageUrl.isNullOrBlank() && imageUrl != AlbumArtCache.PLACEHOLDER_PATH && File(imageUrl).exists()
        if (isValid) {
            try {
                nvg.drawRoundedImage(File(imageUrl), x, y, size, size, radius)
                return
            } catch (e: Exception) {
                ShindoLogger.error("[MUSIC] Failed to draw small art: $imageUrl", e)
            }
        }
        nvg.drawRoundedRect(x, y, size, size, radius, Color(50, 50, 50))
        try {
            nvg.drawRoundedImage(PLACEHOLDER_IMAGE, x, y, size, size, radius)
        } catch (e: Exception) {
            ShindoLogger.error("[MUSIC] Failed to draw small art.", e)
        }
    }

    companion object {
        fun formatDuration(ms: Long): String {
            val s = ms / 1000L
            return "%d:%02d".format(s / 60, s % 60)
        }

        fun formatFollowers(count: Long): String =
            when {
                count >= 1_000_000 -> "%.1fM".format(count / 1_000_000f)
                count >= 1_000 -> "%.1fK".format(count / 1_000f)
                else -> count.toString()
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
                            if (nvg.getTextWidth("$p$c", fontSize, font) <= maxWidth) {
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
    }
}
