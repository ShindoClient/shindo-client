package me.miki.shindo.gui.modmenu.v2.category.impl.spotify.data

import com.wrapper.spotify.model_objects.specification.PlaylistSimplified
import com.wrapper.spotify.model_objects.specification.Track

data class SearchSnapshot(
    val tracks: List<Track> = emptyList(),
    val playlists: List<PlaylistSimplified> = emptyList(),
)
