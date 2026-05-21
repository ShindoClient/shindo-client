package me.miki.shindo.gui.modmenu.v2.category.impl.spotify

import com.wrapper.spotify.model_objects.specification.AlbumSimplified
import com.wrapper.spotify.model_objects.specification.ArtistSimplified
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified

sealed class SpotifyScreen {
    object Library : SpotifyScreen()

    data class PlaylistDetail(
        val playlist: PlaylistSimplified,
        val playlistId: String,
    ) : SpotifyScreen()

    data class ArtistDetail(
        val artist: ArtistSimplified,
    ) : SpotifyScreen()

    data class AlbumDetail(
        val album: AlbumSimplified,
        val albumId: String,
    ) : SpotifyScreen()

    object Lyrics : SpotifyScreen()
}
