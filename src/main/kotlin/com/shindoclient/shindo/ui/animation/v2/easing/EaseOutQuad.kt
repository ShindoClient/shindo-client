package com.shindoclient.shindo.ui.animation.v2.easing

import com.shindoclient.shindo.ui.animation.v2.Animation

class EaseOutQuad(
    ms: Int,
    endPoint: Double,
) : Animation(ms, endPoint) {
    init {
        this.reset()
    }

    protected override fun getEquation(x: Double): Double {
        val x1: Double = x / getDuration()
        return -1 * x1 * (x1 - 2)
    }
}
