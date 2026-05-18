package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventRender2D(
    @JvmField val partialTicks: Float,
) : Event() {
    fun getPartialTicks(): Float = partialTicks
}
