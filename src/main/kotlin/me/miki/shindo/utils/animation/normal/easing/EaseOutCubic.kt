package me.miki.shindo.utils.animation.normal.easing

import me.miki.shindo.utils.animation.normal.Animation

class EaseOutCubic(ms: Int, endPoint: Double) : Animation(ms, endPoint) {

    override fun getEquation(x: Double): Double {
        var x1 = x / duration
        x1--
        return x1 * x1 * x1 + 1
    }
}
