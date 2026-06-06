package com.shindoclient.shindo.management.music

interface TrackInfoCallback {
    fun onTrackInfoUpdated(
        position: Long,
        duration: Long,
    )
}
