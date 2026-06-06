package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event

class EventPlaySound(
    private val soundName: String,
    private var volume: Float,
    private var pitch: Float,
    private var originalVolume: Float,
    private var originalPitch: Float,
) : Event() {
    fun getVolume(): Float = volume

    fun getPitch(): Float = pitch

    fun getOriginalVolume(): Float = originalVolume

    fun getOriginalPitch(): Float = originalPitch

    fun getSoundName(): String = soundName

    fun setVolume(volume: Float) {
        this.volume = volume
    }

    fun setPitch(pitch: Float) {
        this.pitch = pitch
    }

    fun setOriginalVolume(originalVolume: Float) {
        this.originalVolume = originalVolume
    }

    fun setOriginalPitch(originalPitch: Float) {
        this.originalPitch = originalPitch
    }
}
