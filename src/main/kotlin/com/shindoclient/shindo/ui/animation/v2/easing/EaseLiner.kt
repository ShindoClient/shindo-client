package com.shindoclient.shindo.ui.animation.v2.easing

import com.shindoclient.shindo.ui.animation.v2.Animation

class EaseLiner(
    ms: Int,
    endPoint: Double,
) : Animation(ms, endPoint) {
    init {
        this.reset()
    }

    protected override fun getEquation(x: Double): Double = x / getDuration()
}
