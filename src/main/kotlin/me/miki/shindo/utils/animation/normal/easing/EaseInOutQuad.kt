package me.miki.shindo.utils.animation.normal.easing

import me.miki.shindo.utils.animation.normal.Animation

class EaseInOutQuad(ms: Int, endPoint: Double) : Animation(ms, endPoint) {

    init {
        this.reset()
    }

    override fun getEquation(x: Double): Double {
        var x1 = x / (duration / 2.0)
        if (x1 < 1) {
            return 0.5 * x1 * x1
        }

        x1--
        return -0.5 * (x1 * (x1 - 2) - 1)
    }
}
