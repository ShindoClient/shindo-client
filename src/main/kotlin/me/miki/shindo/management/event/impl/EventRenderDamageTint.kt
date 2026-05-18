package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventRenderDamageTint(
    private val _partialTicks: Float,
) : Event() {
    fun getPartialTicks(): Float = _partialTicks
}
