package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventRenderTooltip(
    private val partialTicks: Float,
) : Event() {
    fun getPartialTicks(): Float = partialTicks
}
