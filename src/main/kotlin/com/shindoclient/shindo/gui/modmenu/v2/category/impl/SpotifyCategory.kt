package com.shindoclient.shindo.gui.modmenu.v2.category.impl

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.gui.modmenu.v2.GuiModMenu
import com.shindoclient.shindo.gui.modmenu.v2.category.Category
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.content.SpotifyContentRenderer
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.input.SpotifyInputHandler
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.nav.SpotifyNavigator
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.nav.SpotifyScreen
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.player.SpotifyPlayerBar
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.search.SpotifySearchManager
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.state.ContentState
import com.shindoclient.shindo.management.color.AccentColor
import com.shindoclient.shindo.management.color.palette.ColorPalette
import com.shindoclient.shindo.management.color.palette.ColorType
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.impl.InternalSettingsMod
import com.shindoclient.shindo.management.music.MusicManager
import com.shindoclient.shindo.management.music.TrackInfoCallback
import com.shindoclient.shindo.management.music.data.ArtistContent
import com.shindoclient.shindo.management.music.data.SearchSnapshot
import com.shindoclient.shindo.management.music.data.TrackListContent
import com.shindoclient.shindo.management.nanovg.NanoVGManager
import com.shindoclient.shindo.management.nanovg.font.Fonts
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.ui.components.v2.inputs.CompSlider
import com.shindoclient.shindo.utils.mouse.MouseUtils
import com.shindoclient.shindo.utils.mouse.Scroll
import java.awt.Color
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

private const val CONTROL_BAR_H = 46f

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

    // Shared mutable state
    val searchSnapshot = AtomicReference<SearchSnapshot?>(null)
    private val trackListState = AtomicReference<ContentState<TrackListContent>>(ContentState.Idle)
    private val artistState = AtomicReference<ContentState<ArtistContent>>(ContentState.Idle)
    private val libraryScroll = Scroll()
    private val detailScroll = Scroll()
    private val lyricsScroll = Scroll()
    private var trackPosition = 0L
    private var trackDuration = 0L
    private var showConnectButton = true
    private var currentTrackId: String? = null

    // Delegates
    private val searchManager = SpotifySearchManager(
        searchSnapshot = searchSnapshot,
        getSearchQuery = { parentRef.get()?.getSearchBox()?.getText() },
        libraryScroll = libraryScroll,
        getScrollAreaH = { getHeight() - CONTROL_BAR_H },
    )

    private val playerBar = SpotifyPlayerBar(
        getX = { getX().toFloat() },
        getY = { getY().toFloat() },
        getWidth = { getWidth().toFloat() },
        getHeight = { getHeight().toFloat() },
        volumeSlider = volumeSlider,
        getTrackPosition = { trackPosition },
        getTrackDuration = { trackDuration },
        navigator = navigator,
    )

    private val contentRenderer = SpotifyContentRenderer(
        getX = { getX().toFloat() },
        getY = { getY().toFloat() },
        getWidth = { getWidth().toFloat() },
        getHeight = { getHeight().toFloat() },
        navigator = navigator,
        trackListState = trackListState,
        artistState = artistState,
        libraryScroll = libraryScroll,
        detailScroll = detailScroll,
        lyricsScroll = lyricsScroll,
        playerBar = playerBar,
        getSearchSnapshot = { searchSnapshot.get() },
        onUpdateLibraryScroll = { searchManager.updateLibraryScroll(it) },
    )

    private val inputHandler = SpotifyInputHandler(
        getX = { getX().toFloat() },
        getY = { getY().toFloat() },
        getWidth = { getWidth().toFloat() },
        getHeight = { getHeight().toFloat() },
        navigator = navigator,
        searchSnapshot = searchSnapshot,
        trackListState = trackListState,
        artistState = artistState,
        libraryScroll = libraryScroll,
        detailScroll = detailScroll,
        lyricsScroll = lyricsScroll,
        volumeSlider = volumeSlider,
        getTrackDuration = { trackDuration },
        parentRef = parentRef,
        getCurrentHighlightedLyricIndex = { contentRenderer.currentHighlightedLyricIndex },
        isShowConnectButton = { showConnectButton },
        setShowConnectButton = { showConnectButton = it },
    )

    init {
        volumeSlider.setCircle(false)
        volumeSlider.setShowValue(false)
    }

    override fun initGui() {}

    override fun initCategory() {
        navigator.reset()
        listOf(libraryScroll, detailScroll, lyricsScroll).forEach { it.resetAll() }
        trackListState.set(ContentState.Idle)
        artistState.set(ContentState.Idle)

        val mm = Shindo.getInstance().getMusicManager()
        showConnectButton = !mm.isAuthorized()
        mm.setTrackInfoCallback(this)

        if (!showConnectButton) {
            inputHandler.syncVolumeAsync(mm)
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

        searchManager.checkAndUpdateSearch()

        if (showConnectButton) {
            drawConnectButton(nvg, palette, accentColor, mouseX, mouseY)
            return
        }

        nvg.save()
        try {
            when (val screen = navigator.current) {
                is SpotifyScreen.Library -> {
                    contentRenderer.drawLibraryLayout(
                        nvg, palette, accentColor, mm, mouseX, mouseY, partialTicks,
                    )
                }

                is SpotifyScreen.Lyrics -> {
                    lyricsScroll.onScroll()
                    lyricsScroll.onAnimation()
                    contentRenderer.drawLyricsView(nvg, palette, accentColor, mm, mouseX, mouseY)
                }

                is SpotifyScreen.PlaylistDetail -> {
                    detailScroll.onScroll()
                    detailScroll.onAnimation()
                    contentRenderer.drawPlaylistDetailScreen(nvg, palette, accentColor, mm, screen, mouseX, mouseY)
                }

                is SpotifyScreen.ArtistDetail -> {
                    detailScroll.onScroll()
                    detailScroll.onAnimation()
                    contentRenderer.drawArtistDetailScreen(nvg, palette, accentColor, mm, screen, mouseX, mouseY)
                }

                is SpotifyScreen.AlbumDetail -> {
                    detailScroll.onScroll()
                    detailScroll.onAnimation()
                    contentRenderer.drawAlbumDetailScreen(nvg, palette, accentColor, mm, screen, mouseX, mouseY)
                }
            }
        } finally {
            nvg.restore()
        }
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        inputHandler.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        inputHandler.mouseReleased(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        inputHandler.keyTyped(typedChar, keyCode)
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
            bx, by, bw, bh, 8f,
            if (hovered) accentColor.getInterpolateColor() else palette.getBackgroundColor(ColorType.DARK),
        )
        val text = TranslateText.SPOTIFY_CONNECT.getText()
        val tw = nvg.getTextWidth(text, 11f, Fonts.MEDIUM)
        val startX = cx - (16f + 8f + tw) / 2f
        val col = if (hovered) Color.WHITE else palette.getFontColor(ColorType.DARK)
        nvg.drawText(Lucide.MUSIC, startX, by + bh / 2f - 8f, col, 16f, Fonts.LUCIDE)
        nvg.drawText(text, startX + 24f, by + bh / 2f - 3f, col, 11f, Fonts.MEDIUM)
    }
}
