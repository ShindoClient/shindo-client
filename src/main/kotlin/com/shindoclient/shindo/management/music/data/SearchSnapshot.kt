package com.shindoclient.shindo.management.music.data

import com.shindoclient.spotify.data.PlaylistSimplified
import com.shindoclient.spotify.data.Track

data class SearchSnapshot(
    val tracks: List<Track> = emptyList(),
    val playlists: List<PlaylistSimplified> = emptyList(),
)
