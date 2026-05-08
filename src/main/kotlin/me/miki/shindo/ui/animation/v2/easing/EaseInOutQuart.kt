package me.miki.shindo.ui.animation.v2.easing

import me.miki.shindo.ui.animation.v2.Animation

class EaseInOutQuart(ms: Int, endPoint: Double) : Animation(ms, endPoint) {
    init {
        this.reset()
    }

    protected override fun getEquation(x: Double): Double {
        var x1: Double = x / (getDuration() / 2)

        if (x1 < 1) {
            return 0.5 * x1 * x1 * x1 * x1
        }

        x1 -= 2.0

        return -0.5 * (x1 * x1 * x1 * x1 - 2)
    }
}