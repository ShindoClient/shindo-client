package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventPlayerHeadRotation(
    private val _yaw: Float,
    private val _pitch: Float,
) : Event() {
    fun getYaw(): Float = _yaw

    fun getPitch(): Float = _pitch
}
