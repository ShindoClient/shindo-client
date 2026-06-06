package com.shindoclient.shindo.ui.animation.v2.easing

import com.shindoclient.shindo.ui.animation.v2.Animation
import kotlin.math.cos

class EaseInSine(
    ms: Int,
    endPoint: Double,
) : Animation(ms, endPoint) {
    init {
        this.reset()
    }

    protected override fun getEquation(x: Double): Double = -1 * cos(x / getDuration() * (Math.PI / 2)) + 1
}
