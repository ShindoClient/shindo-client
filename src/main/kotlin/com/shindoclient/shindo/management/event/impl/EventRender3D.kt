package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event

class EventRender3D(
    private val partialTicks: Float,
) : Event() {
    fun getPartialTicks(): Float = partialTicks
}
