package me.miki.shindo.gui.modmenu.v2.category.impl.spotify.data

import com.wrapper.spotify.model_objects.specification.Track

data class ArtistContent(
    val topTracks: List<Track>,
    val imageUrl: String?,
    val followerCount: Long,
    val genres: List<String>,
)
