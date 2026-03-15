package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventRender2D(partialTicks: Float) : Event() {
    @JvmField
    val partialTicks: Float = partialTicks

    fun getPartialTicks(): Float = partialTicks
}

