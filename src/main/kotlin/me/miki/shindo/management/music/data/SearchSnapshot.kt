package me.miki.shindo.management.music.data

import com.wrapper.spotify.model_objects.specification.PlaylistSimplified
import com.wrapper.spotify.model_objects.specification.Track

data class SearchSnapshot(
    val tracks: List<Track> = emptyList(),
    val playlists: List<PlaylistSimplified> = emptyList(),
)
