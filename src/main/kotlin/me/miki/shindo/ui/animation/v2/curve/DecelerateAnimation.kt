package me.miki.shindo.ui.animation.v2.curve

import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction


open class DecelerateAnimation : Animation {
    constructor(ms: Int, endPoint: Double) : super(ms, endPoint) {
        this.reset()
    }

    constructor(ms: Int, endPoint: Double, direction: Direction) : super(ms, endPoint, direction) {
        this.reset()
    }

    override fun getEquation(x: Double): Double {
        val x1: Double = x / getDuration()
        return 1 - ((x1 - 1) * (x1 - 1))
    }
}