@file:JvmName("MusicManagerExtensions")

package com.shindoclient.extensions.music

import com.shindoclient.spotify.data.Track
import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.management.music.MusicManager
import com.shindoclient.shindo.management.music.data.ArtistContent
import com.shindoclient.shindo.management.music.data.TrackListContent
import java.util.concurrent.CompletableFuture

fun MusicManager.getPlaylistTracks(playlistId: String): CompletableFuture<TrackListContent> =
    getSpotify().playlist.getAllPlaylistTracks(playlistId)
        .thenApply { playlistTracks ->
            val tracks = playlistTracks.mapNotNull { it.track }
            tracks.forEach { getAlbumArtUrl(it) }
            TrackListContent(tracks = tracks, totalCount = tracks.size)
        }
        .exceptionally { ex ->
            ShindoLogger.error("Failed to load playlist tracks: ${ex.message}", ex)
            TrackListContent(tracks = emptyList(), totalCount = 0)
        }

fun MusicManager.getArtistContent(artistId: String): CompletableFuture<ArtistContent> {
    val spotify = getSpotify()
    return spotify.artist.getArtist(artistId)
        .thenCompose { artist ->
            spotify.artist.getTopTracks(artistId)
                .thenApply { topTracks ->
                    topTracks.forEach { getAlbumArtUrl(it) }
                    val imageUrl = artist.images.firstOrNull()?.url?.let { getAlbumArt(it) }
                    ArtistContent(
                        topTracks = topTracks,
                        imageUrl = imageUrl,
                        followerCount = (artist.followers?.total ?: 0).toLong(),
                        genres = artist.genres,
                    )
                }
        }
        .exceptionally { ex ->
            ShindoLogger.error("Failed to load artist content: ${ex.message}", ex)
            ArtistContent(topTracks = emptyList(), imageUrl = null, followerCount = 0L, genres = emptyList())
        }
}

fun MusicManager.getAlbumTracks(albumId: String): CompletableFuture<TrackListContent> =
    CompletableFuture.supplyAsync {
        try {
            val spotify = getSpotify()
            val allTracks = mutableListOf<Track>()
            var offset = 0
            var total = Int.MAX_VALUE

            while (allTracks.size < total && allTracks.size < 200) {
                val page = spotify.album.getAlbumTracks(albumId, 50, offset).get()
                if (page.isEmpty()) break

                val ids = page.mapNotNull { it.id }.take(50)
                if (ids.isNotEmpty()) {
                    val converted = spotify.album.getSeveralTracks(ids).get()
                    allTracks.addAll(converted)
                    converted.forEach { getAlbumArtUrl(it) }
                }

                offset += page.size
                Thread.sleep(100L)
            }

            TrackListContent(tracks = allTracks, totalCount = total)
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load album tracks: ${e.message}", e)
            TrackListContent(tracks = emptyList(), totalCount = 0)
        }
    }
