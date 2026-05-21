package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventPlayerHeadRotation(
    private val yaw: Float,
    private val pitch: Float,
) : Event() {
    fun getYaw(): Float = yaw

    fun getPitch(): Float = pitch
}
