package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventGamma(
    private var _gamma: Float,
) : Event() {
    fun getGamma(): Float = _gamma

    fun setGamma(gamma: Float) {
        _gamma = gamma
    }
}
