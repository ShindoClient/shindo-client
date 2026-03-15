package me.miki.shindo.management.music

interface TrackInfoCallback {
    fun onTrackInfoUpdated(position: Long, duration: Long)
}
