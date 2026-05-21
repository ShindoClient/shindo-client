package me.miki.shindo.gui.modmenu.v2.category.impl.spotify.data

import com.wrapper.spotify.model_objects.specification.Track

data class TrackListContent(
    val tracks: List<Track>,
    val totalCount: Int,
)
