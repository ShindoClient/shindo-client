package me.miki.shindo.ui.animation.v2.easing

import me.miki.shindo.ui.animation.v2.Animation

class EaseOutQuint(
    ms: Int,
    endPoint: Double,
) : Animation(ms, endPoint) {
    init {
        this.reset()
    }

    protected override fun getEquation(x: Double): Double {
        var x1: Double = x / getDuration()
        x1--
        return x1 * x1 * x1 * x1 * x1 + 1
    }
}