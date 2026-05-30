package me.miki.shindo.management.music.data

import com.wrapper.spotify.model_objects.specification.Track

data class ArtistContent(
    val topTracks: List<Track>,
    val imageUrl: String?,
    val followerCount: Long,
    val genres: List<String>,
)
