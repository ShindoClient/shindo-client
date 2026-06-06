package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event

class EventGamma(
    private var gamma: Float,
) : Event() {
    fun getGamma(): Float = gamma

    fun setGamma(gamma: Float) {
        this.gamma = gamma
    }
}
