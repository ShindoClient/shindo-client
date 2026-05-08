package me.miki.shindo.ui.animation.v2.easing

import me.miki.shindo.ui.animation.v2.Animation
import kotlin.math.cos

class EaseInOutSine(ms: Int, endPoint: Double) : Animation(ms, endPoint) {
    init {
        this.reset()
    }

    protected override fun getEquation(x: Double): Double {
        return -0.5 * (cos(Math.PI * x / getDuration()) - 1)
    }
}