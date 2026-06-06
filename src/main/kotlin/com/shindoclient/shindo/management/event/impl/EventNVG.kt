package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.event.Event
import com.shindoclient.shindo.management.nanovg.NanoVGManager

class EventNVG(
    val partialTicks: Float,
) : Event() {
    fun renderer(): NanoVGManager = Shindo.getInstance().nanoVGManager
}
