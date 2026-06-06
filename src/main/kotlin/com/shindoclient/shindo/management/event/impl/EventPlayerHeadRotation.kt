package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event

class EventPlayerHeadRotation(
    private val yaw: Float,
    private val pitch: Float,
) : Event() {
    fun getYaw(): Float = yaw

    fun getPitch(): Float = pitch
}
