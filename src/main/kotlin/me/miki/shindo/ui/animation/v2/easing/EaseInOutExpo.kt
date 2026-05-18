package me.miki.shindo.ui.animation.v2.easing

import me.miki.shindo.ui.animation.v2.Animation
import kotlin.math.pow

class EaseInOutExpo(
    ms: Int,
    endPoint: Double,
) : Animation(ms, endPoint) {
    init {
        this.reset()
    }

    protected override fun getEquation(x: Double): Double {
        if (x == 0.0) {
            return 0.0
        }

        if (x.toInt() == getDuration()) {
            return 1.0
        }

        var x1: Double = x / (getDuration() / 2)

        if (x1 < 1) {
            return 0.5 * 2.0.pow(10 * (x1 - 1))
        }

        x1--

        return 0.5 * ((-2.0).pow(-10 * x1) + 2)
    }
}
