package com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.search

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.music.data.SearchSnapshot
import com.shindoclient.shindo.management.notification.NotificationType
import com.shindoclient.shindo.utils.mouse.Scroll
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

private const val SEARCH_DEBOUNCE_DELAY_MS = 800L

class SpotifySearchManager(
    private val searchSnapshot: AtomicReference<SearchSnapshot?>,
    private val getSearchQuery: () -> String?,
    private val libraryScroll: Scroll,
    private val getScrollAreaH: () -> Float,
) {
    private val searchDebouncer: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "Search-Debouncer").apply { isDaemon = true }
        }
    private val isSearching = AtomicBoolean(false)
    private var pendingSearch: ScheduledFuture<*>? = null
    private var lastSearchQuery = ""

    fun checkAndUpdateSearch() {
        val query = getSearchQuery() ?: return
        if (query != lastSearchQuery) {
            scheduleSearch(query)
            lastSearchQuery = query
        }
    }

    fun scheduleSearch(query: String) {
        if (query.isEmpty()) {
            searchSnapshot.set(null)
            return
        }
        pendingSearch?.takeIf { !it.isDone }?.cancel(false)
        pendingSearch =
            searchDebouncer.schedule({
                if (!isSearching.compareAndSet(false, true)) return@schedule
                val mm = Shindo.getInstance().getMusicManager()
                mm.searchTracks(query)
                    .thenCombine(mm.searchPlaylists(query)) { tracks, playlists ->
                        SearchSnapshot(
                            tracks = tracks ?: emptyList(),
                            playlists = playlists ?: emptyList(),
                        )
                    }
                    .thenAccept { snapshot ->
                        snapshot.tracks.take(5).forEach { mm.getAlbumArtUrl(it) }
                        snapshot.playlists.take(5).forEach { mm.getPlaylistImageUrl(it) }
                        searchSnapshot.set(snapshot)
                        updateLibraryScroll(snapshot)
                    }
                    .exceptionally { ex ->
                        ShindoLogger.error("Search failed", ex)
                        Shindo.getInstance().getNotificationManager().post(
                            TranslateText.MUSIC,
                            TranslateText.SPOTIFY_SEARCH_FAILED,
                            NotificationType.ERROR,
                        )
                        null
                    }
                    .whenComplete { _, _ -> isSearching.set(false) }
            }, SEARCH_DEBOUNCE_DELAY_MS, TimeUnit.MILLISECONDS)
    }

    fun updateLibraryScroll(snapshot: SearchSnapshot?) {
        val count = (snapshot?.tracks?.size ?: 0) + (snapshot?.playlists?.size ?: 0)
        val scrollAreaH = getScrollAreaH()
        libraryScroll.maxScroll = max(0f, count * ENTRY_HEIGHT - scrollAreaH + 20f)
    }

    fun isEntryVisible(
        offsetY: Float,
        clipTop: Float,
        clipH: Float,
    ): Boolean {
        val scrolled = -libraryScroll.getValue()
        return offsetY + ENTRY_ITEM_H >= scrolled && offsetY <= scrolled + clipH
    }

    companion object {
        private const val ENTRY_HEIGHT = 56f
        private const val ENTRY_ITEM_H = 46f
    }
}
