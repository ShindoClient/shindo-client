package com.shindoclient.shindo.management.music.data

import com.shindoclient.spotify.data.Track

data class TrackListContent(
    val tracks: List<Track>,
    val totalCount: Int,
)
