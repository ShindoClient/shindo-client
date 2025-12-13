package me.miki.shindo.utils.animation.normal.other

import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import kotlin.math.pow

class SmoothStepAnimation : Animation {

    constructor(ms: Int, endPoint: Double) : super(ms, endPoint) {
        reset()
    }

    constructor(ms: Int, endPoint: Double, direction: Direction) : super(ms, endPoint, direction) {
        reset()
    }

    override fun getEquation(x: Double): Double {
        val x1 = x / duration.toDouble()
        return -2 * x1.pow(3) + 3 * x1.pow(2)
    }
}