package com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.input
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.nav.SpotifyNavigator
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.nav.SpotifyScreen
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.state.ContentState

import com.shindoclient.spotify.data.AlbumSimplified
import com.shindoclient.spotify.data.ArtistSimplified
import com.shindoclient.spotify.data.PlaylistSimplified
import com.shindoclient.spotify.data.Track
import com.shindoclient.extensions.music.getAlbumTracks
import com.shindoclient.extensions.music.getArtistContent
import com.shindoclient.extensions.music.getPlaylistTracks
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.gui.modmenu.v2.GuiModMenu
import com.shindoclient.shindo.management.music.MusicManager
import com.shindoclient.shindo.management.music.data.ArtistContent
import com.shindoclient.shindo.management.music.data.SearchSnapshot
import com.shindoclient.shindo.management.music.data.TrackListContent
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.notification.NotificationType
import com.shindoclient.shindo.ui.components.v2.inputs.CompSlider
import com.shindoclient.shindo.utils.BrowserUtils
import com.shindoclient.shindo.utils.mouse.MouseUtils
import com.shindoclient.shindo.utils.mouse.Scroll
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiConfirmOpenLink
import org.lwjgl.input.Keyboard
import java.lang.ref.WeakReference
import java.util.concurrent.CompletableFuture
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
private const val VOLUME_CHANGE_DELAY_MS = 500L

class SpotifyInputHandler(
    private val getX: () -> Float,
    private val getY: () -> Float,
    private val getWidth: () -> Float,
    private val getHeight: () -> Float,
    private val navigator: SpotifyNavigator,
    private val searchSnapshot: AtomicReference<SearchSnapshot?>,
    private val getUserPlaylists: () -> List<PlaylistSimplified>?,
    private val trackListState: AtomicReference<ContentState<TrackListContent>>,
    private val artistState: AtomicReference<ContentState<ArtistContent>>,
    private val libraryScroll: Scroll,
    private val sidebarScroll: Scroll,
    private val detailScroll: Scroll,
    private val lyricsScroll: Scroll,
    private val volumeSlider: CompSlider,
    private val getTrackDuration: () -> Long,
    private val parentRef: WeakReference<GuiModMenu>,
    private val getCurrentHighlightedLyricIndex: () -> Int,
    private val isShowConnectButton: () -> Boolean,
    private val setShowConnectButton: (Boolean) -> Unit,
) {
    private var lastVolumeChangeTime = 0L

    fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (!MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight().toFloat())) return
        if (isShowConnectButton()) {
            if (mouseButton == 0 &&
                MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() / 2f - 75f, getY() + getHeight() / 2f - 20f, 150f, 40f)
            ) {
                openConfirmDialog(Shindo.getInstance().getMusicManager().getAuthorizationCodeUri())
                setShowConnectButton(false)
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

    fun mouseReleased(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (!isShowConnectButton()) {
            volumeSlider.mouseReleased(mouseX, mouseY, mouseButton)
            updateVolume()
        }
    }

    fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        if (isShowConnectButton()) return
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
            Keyboard.KEY_RIGHT -> mm.seekToPosition(min(mm.getTrackPosition() + 10_000, getTrackDuration()))
            Keyboard.KEY_LEFT -> mm.seekToPosition(max(mm.getTrackPosition() - 10_000, 0))
        }
        when (navigator.current) {
            is SpotifyScreen.Lyrics -> lyricsScroll.onKey(keyCode)
            is SpotifyScreen.PlaylistDetail, is SpotifyScreen.ArtistDetail, is SpotifyScreen.AlbumDetail -> detailScroll.onKey(keyCode)
            else -> libraryScroll.onKey(keyCode)
        }
    }

    fun syncVolumeAsync(mm: MusicManager) {
        CompletableFuture.runAsync {
            try {
                mm.fetchAndUpdateVolume()
                volumeSlider.getSetting().setValue(mm.getVolume() / 100.0)
            } catch (e: Exception) {
                // Volume sync failed silently
            }
        }
    }

    private fun handleLibraryClick(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton != 0) return
        val mm = Shindo.getInstance().getMusicManager()

        // Lyrics button
        val bx = getX() + getWidth() - 116f
        val by = getY() + getHeight() - 26f
        if (MouseUtils.isInside(mouseX, mouseY, bx, by, 16f, 16f)) {
            navigator.push(SpotifyScreen.Lyrics)
            lyricsScroll.resetAll()
            mm.getCurrentTrack()?.let { mm.getLyricsManager().fetchLyrics(it) }
            return
        }

        // Sidebar playlist click
        if (MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), SIDEBAR_WIDTH, getHeight() - CONTROL_BAR_H)) {
            val playlists = getUserPlaylists() ?: return
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

        // Search results area
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
                    track.album?.let { album -> album.id?.let { navigateToAlbum(album, it) } }
                    return
                }
                mm.play(track.uri)
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
                        mm.playPlaylist(uri)
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
        val mm = Shindo.getInstance().getMusicManager()
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
                    track.album?.let { album -> album.id?.let { navigateToAlbum(album, it) } }
                    return
                }
                mm.play(track.uri)
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
        val highlightedIndex = getCurrentHighlightedLyricIndex()
        if (highlightedIndex >= 0) {
            val mm = Shindo.getInstance().getMusicManager()
            val lyrics = mm.getLyricsManager().getCurrentLyrics()
            if (lyrics != null && !lyrics.isError() && highlightedIndex < lyrics.lines.size) {
                mm.seekToPosition(lyrics.lines[highlightedIndex].startTime)
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
                    mm.seekToPosition(((mouseX - (getX() + 20f)) / (getWidth() - 40f) * getTrackDuration()).toLong())
                }
            }
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
        album: AlbumSimplified,
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
}
