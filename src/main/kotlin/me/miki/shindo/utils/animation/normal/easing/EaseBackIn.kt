package me.miki.shindo.utils.animation.normal.easing

import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import kotlin.math.max
import kotlin.math.pow

class EaseBackIn : Animation {

    private val easeAmount: Float

    constructor(ms: Int, endPoint: Double, easeAmount: Float) : super(ms, endPoint) {
        this.easeAmount = easeAmount
        this.reset()
    }

    constructor(ms: Int, endPoint: Double, easeAmount: Float, direction: Direction) : super(ms, endPoint, direction) {
        this.easeAmount = easeAmount
        this.reset()
    }

    override fun correctOutput(): Boolean {
        return true
    }

    override fun getEquation(x: Double): Double {
        val x1 = x / duration
        val shrink = easeAmount + 1
        return max(0.0, 1 + shrink * (x1 - 1).pow(3) + easeAmount * (x1 - 1).pow(2))
    }
}