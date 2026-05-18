package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventPlaySound(
    private val _soundName: String,
    private var _volume: Float,
    private var _pitch: Float,
    private var _originalVolume: Float,
    private var _originalPitch: Float,
) : Event() {
    fun getVolume(): Float = _volume

    fun setVolume(volume: Float) {
        _volume = volume
    }

    fun getPitch(): Float = _pitch

    fun setPitch(pitch: Float) {
        _pitch = pitch
    }

    fun getOriginalVolume(): Float = _originalVolume

    fun setOriginalVolume(originalVolume: Float) {
        _originalVolume = originalVolume
    }

    fun getOriginalPitch(): Float = _originalPitch

    fun setOriginalPitch(originalPitch: Float) {
        _originalPitch = originalPitch
    }

    fun getSoundName(): String = _soundName
}
