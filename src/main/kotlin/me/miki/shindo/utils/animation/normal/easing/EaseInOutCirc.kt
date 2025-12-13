package me.miki.shindo.utils.animation.normal.easing

import me.miki.shindo.utils.animation.normal.Animation
import kotlin.math.sqrt

class EaseInOutCirc(ms: Int, endPoint: Double) : Animation(ms, endPoint) {

    init {
        this.reset()
    }

    override fun getEquation(x: Double): Double {
        val x1 = x / duration - 1
        return sqrt(1 - x1 * x1)
    }
}
