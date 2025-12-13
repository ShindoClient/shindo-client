package me.miki.shindo.utils.animation.normal.easing

import me.miki.shindo.utils.animation.normal.Animation
import kotlin.math.pow

class EaseInOutExpo(ms: Int, endPoint: Double) : Animation(ms, endPoint) {

    init {
        this.reset()
    }

    override fun getEquation(x: Double): Double {
        if (x == 0.0) {
            return 0.0
        }
        if (x == duration) {
            return 1.0
        }

        var x1 = x / (duration / 2.0)
        if (x1 < 1) {
            return 0.5 * 2.0.pow(10 * (x1 - 1))
        }

        x1--
        return 0.5 * (-2.0.pow(-10 * x1) + 2)
    }
}
