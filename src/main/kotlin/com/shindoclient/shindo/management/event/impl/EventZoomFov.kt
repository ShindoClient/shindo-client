package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event

class EventZoomFov(
    private var fov: Float,
) : Event() {
    fun getFov(): Float = fov

    fun setFov(fov: Float) {
        this.fov = fov
    }
}
