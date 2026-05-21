package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventGamma(
    private var gamma: Float,
) : Event() {
    fun getGamma(): Float = gamma

    fun setGamma(gamma: Float) {
        this.gamma = gamma
    }
}
