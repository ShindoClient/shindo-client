package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventZoomFov(
    private var fov: Float,
) : Event() {
    fun getFov(): Float = fov

    fun setFov(fov: Float) {
        this.fov = fov
    }
}
