package com.shindoclient.shindo.ui.animation.v2.easing

import com.shindoclient.shindo.ui.animation.v2.Animation
import kotlin.math.sqrt

class EaseInCirc(
    ms: Int,
    endPoint: Double,
) : Animation(ms, endPoint) {
    init {
        this.reset()
    }

    protected override fun getEquation(x: Double): Double {
        val x1 = x / getDuration()
        return -1 * (sqrt(1 - x1 * x1) - 1)
    }
}
