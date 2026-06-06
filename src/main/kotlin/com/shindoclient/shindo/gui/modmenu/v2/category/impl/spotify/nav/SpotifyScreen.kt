package com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.nav

import com.shindoclient.spotify.data.AlbumSimplified
import com.shindoclient.spotify.data.ArtistSimplified
import com.shindoclient.spotify.data.PlaylistSimplified

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
