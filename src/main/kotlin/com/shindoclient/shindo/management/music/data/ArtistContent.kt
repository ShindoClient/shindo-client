package com.shindoclient.shindo.management.music.data

import com.shindoclient.spotify.data.Track

data class ArtistContent(
    val topTracks: List<Track>,
    val imageUrl: String?,
    val followerCount: Long,
    val genres: List<String>,
)
