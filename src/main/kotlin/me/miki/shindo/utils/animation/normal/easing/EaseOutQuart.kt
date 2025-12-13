package me.miki.shindo.utils.animation.normal.easing

import me.miki.shindo.utils.animation.normal.Animation

class EaseOutQuart(ms: Int, endPoint: Double) : Animation(ms, endPoint) {

    init {
        this.reset()
    }

    override fun getEquation(x: Double): Double {
        var x1 = x / duration
        x1--
        return -1 * (x1 * x1 * x1 * x1 - 1)
    }
}
