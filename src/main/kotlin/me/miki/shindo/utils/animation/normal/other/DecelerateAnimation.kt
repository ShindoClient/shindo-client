package me.miki.shindo.utils.animation.normal.other

import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction

class DecelerateAnimation : Animation {

    constructor(ms: Int, endPoint: Double) : super(ms, endPoint) {
        reset()
    }

    constructor(ms: Int, endPoint: Double, direction: Direction) : super(ms, endPoint, direction) {
        reset()
    }

    override fun getEquation(x: Double): Double {
        val x1 = x / duration
        return 1 - ((x1 - 1) * (x1 - 1))
    }
}