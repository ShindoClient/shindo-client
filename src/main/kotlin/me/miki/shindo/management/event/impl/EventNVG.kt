package me.miki.shindo.management.event.impl

import me.miki.shindo.Shindo
import me.miki.shindo.management.event.Event
import me.miki.shindo.management.nanovg.NanoVGManager

class EventNVG(
    val partialTicks: Float,
) : Event() {
    fun renderer(): NanoVGManager = Shindo.getInstance().nanoVGManager
}
