package me.miki.shindo.management.music.data

import com.wrapper.spotify.model_objects.specification.Track

data class TrackListContent(
    val tracks: List<Track>,
    val totalCount: Int,
)
