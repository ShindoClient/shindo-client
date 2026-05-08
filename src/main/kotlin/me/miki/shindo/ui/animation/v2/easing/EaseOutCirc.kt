package me.miki.shindo.ui.animation.v2.easing

import me.miki.shindo.ui.animation.v2.Animation
import kotlin.math.sqrt


class EaseOutCirc(ms: Int, endPoint: Double) : Animation(ms, endPoint) {
    init {
        this.reset()
    }

    protected override fun getEquation(x: Double): Double {
        val x1: Double = x / getDuration() - 1
        return sqrt(1 - x1 * x1)
    }
}