package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventZoomFov(
    private var _fov: Float,
) : Event() {
    fun getFov(): Float = _fov

    fun setFov(fov: Float) {
        _fov = fov
    }
}
