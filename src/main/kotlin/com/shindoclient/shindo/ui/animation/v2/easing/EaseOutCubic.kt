package com.shindoclient.shindo.ui.animation.v2.easing

import com.shindoclient.shindo.ui.animation.v2.Animation

class EaseOutCubic(
    ms: Int,
    endPoint: Double,
) : Animation(ms, endPoint) {
    protected override fun getEquation(x: Double): Double {
        var x1: Double = x / getDuration()
        x1--

        return x1 * x1 * x1 + 1
    }
}
