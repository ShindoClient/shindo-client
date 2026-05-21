@file:JvmName("MusicManagerExtensions")

package me.miki.extensions.music

import com.wrapper.spotify.model_objects.specification.Track
import me.miki.shindo.gui.modmenu.v2.category.impl.spotify.data.ArtistContent
import me.miki.shindo.gui.modmenu.v2.category.impl.spotify.data.TrackListContent
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.music.MusicManager
import java.util.concurrent.CompletableFuture

fun MusicManager.getPlaylistTracks(playlistId: String): CompletableFuture<TrackListContent> =
    CompletableFuture.supplyAsync {
        try {
            val spotifyApi = getSpotifyApi()
            val allTracks = mutableListOf<Track>()
            var offset = 0
            var total = Int.MAX_VALUE

            while (allTracks.size < total && allTracks.size < 500) {
                val page =
                    spotifyApi
                        .getPlaylistsItems(playlistId)
                        .limit(50)
                        .offset(offset)
                        .build()
                        .execute()

                total = page.total
                val items = page.items ?: break

                for (item in items) {
                    val track = item.track
                    if (track is Track) allTracks.add(track)
                }

                offset += items.size
                if (items.isEmpty()) break

                allTracks.takeLast(items.size).forEach { getAlbumArtUrl(it) }

                Thread.sleep(50L)
            }

            TrackListContent(tracks = allTracks, totalCount = total)
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load playlist tracks: ${e.message}", e)
            TrackListContent(tracks = emptyList(), totalCount = 0)
        }
    }

fun MusicManager.getArtistContent(artistId: String): CompletableFuture<ArtistContent> =
    CompletableFuture.supplyAsync {
        try {
            val spotifyApi = getSpotifyApi()

            val artist = spotifyApi.getArtist(artistId).build().execute()
            Thread.sleep(50L)

            val topTracks =
                spotifyApi
                    .getArtistsTopTracks(artistId, com.neovisionaries.i18n.CountryCode.US)
                    .build()
                    .execute()
                    .toList()

            topTracks.forEach { getAlbumArtUrl(it) }

            val imageUrl =
                artist.images?.firstOrNull()?.url?.let { url ->
                    getAlbumArt(url)
                }

            ArtistContent(
                topTracks = topTracks,
                imageUrl = imageUrl,
                followerCount = artist.followers?.total?.toLong() ?: 0L,
                genres = artist.genres?.toList() ?: emptyList(),
            )
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load artist content: ${e.message}", e)
            ArtistContent(topTracks = emptyList(), imageUrl = null, followerCount = 0L, genres = emptyList())
        }
    }

fun MusicManager.getAlbumTracks(albumId: String): CompletableFuture<TrackListContent> =
    CompletableFuture.supplyAsync {
        try {
            val spotifyApi = getSpotifyApi()
            val allTracks = mutableListOf<Track>()
            var offset = 0
            var total = Int.MAX_VALUE

            while (allTracks.size < total && allTracks.size < 200) {
                val page =
                    spotifyApi
                        .getAlbumsTracks(albumId)
                        .limit(50)
                        .offset(offset)
                        .build()
                        .execute()

                total = page.total
                val items = page.items ?: break

                val ids = items.mapNotNull { it.id }.take(50).toTypedArray()
                if (ids.isNotEmpty()) {
                    val fullTracks = spotifyApi.getSeveralTracks(*ids).build().execute()
                    allTracks.addAll(fullTracks.toList())
                    fullTracks.forEach { getAlbumArtUrl(it) }
                }

                offset += items.size
                if (items.isEmpty()) break
                Thread.sleep(100L)
            }

            TrackListContent(tracks = allTracks, totalCount = total)
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load album tracks: ${e.message}", e)
            TrackListContent(tracks = emptyList(), totalCount = 0)
        }
    }
