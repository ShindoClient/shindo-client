package me.miki.shindo.utils.animation.normal.easing

import me.miki.shindo.utils.animation.normal.Animation
import kotlin.math.pow

class EaseInExpo(ms: Int, endPoint: Double) : Animation(ms, endPoint) {

    init {
        this.reset()
    }

    override fun getEquation(x: Double): Double {
        return if (x == 0.0) 0.0 else 2.0.pow(10 * (x / duration - 1))
    }
}
