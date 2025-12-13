package me.miki.shindo.utils.animation.normal.easing

import me.miki.shindo.utils.animation.normal.Animation
import kotlin.math.cos

class EaseInSine(ms: Int, endPoint: Double) : Animation(ms, endPoint) {

    init {
        this.reset()
    }

    override fun getEquation(x: Double): Double {
        return -1 * cos(x / duration * (Math.PI / 2)) + 1
    }
}
