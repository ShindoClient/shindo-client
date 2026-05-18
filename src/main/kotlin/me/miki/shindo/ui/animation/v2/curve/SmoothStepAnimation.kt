package me.miki.shindo.ui.animation.v2.curve

import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction
import kotlin.math.pow

open class SmoothStepAnimation : Animation {
    constructor(ms: Int, endPoint: Double) : super(ms, endPoint) {
        this.reset()
    }

    constructor(ms: Int, endPoint: Double, direction: Direction) : super(ms, endPoint, direction) {
        this.reset()
    }

    override fun getEquation(x: Double): Double {
        val x1 = x / getDuration()
        return -2 * x1.pow(3.0) + (3 * x1.pow(2.0))
    }
}
