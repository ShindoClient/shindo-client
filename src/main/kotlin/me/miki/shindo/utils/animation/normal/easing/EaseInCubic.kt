package me.miki.shindo.utils.animation.normal.easing

import me.miki.shindo.utils.animation.normal.Animation

class EaseInCubic(ms: Int, endPoint: Double) : Animation(ms, endPoint) {

    init {
        this.reset()
    }

    override fun getEquation(x: Double): Double {
        val x1 = x / duration
        return x1 * x1 * x1
    }
}
