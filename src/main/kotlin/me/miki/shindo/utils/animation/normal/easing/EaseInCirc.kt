package me.miki.shindo.utils.animation.normal.easing

import me.miki.shindo.utils.animation.normal.Animation
import kotlin.math.sqrt

class EaseInCirc(ms: Int, endPoint: Double) : Animation(ms, endPoint) {

    init {
        this.reset()
    }

    override fun getEquation(x: Double): Double {
        val x1 = x / duration
        return -1 * (sqrt(1 - x1 * x1) - 1)
    }
}
