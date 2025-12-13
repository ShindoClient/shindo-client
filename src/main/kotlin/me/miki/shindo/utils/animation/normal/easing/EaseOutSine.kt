package me.miki.shindo.utils.animation.normal.easing

import me.miki.shindo.utils.animation.normal.Animation
import kotlin.math.sin

class EaseOutSine(ms: Int, endPoint: Double) : Animation(ms, endPoint) {

    init {
        this.reset()
    }

    override fun getEquation(x: Double): Double {
        return sin(x / duration * (Math.PI / 2))
    }
}
