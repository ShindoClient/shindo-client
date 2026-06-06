package com.shindoclient.shindo.ui.animation.v2.easing

import com.shindoclient.shindo.ui.animation.v2.Animation
import kotlin.math.pow

class EaseOutExpo(
    ms: Int,
    endPoint: Double,
) : Animation(ms, endPoint) {
    init {
        this.reset()
    }

    protected override fun getEquation(x: Double): Double =
        if (x.toInt() == getDuration()) 1.0 else ((-2.0).pow(-10 * x / getDuration()) + 1)
}
