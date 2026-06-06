package com.shindoclient.shindo.ui.animation.v2.easing

import com.shindoclient.shindo.ui.animation.v2.Animation
import com.shindoclient.shindo.ui.animation.v2.Direction
import kotlin.math.max
import kotlin.math.pow

class EaseBackIn : Animation {
    private val easeAmount: Float

    constructor(ms: Int, endPoint: Double, easeAmount: Float) : super(ms, endPoint) {
        this.easeAmount = easeAmount
        this.reset()
    }

    constructor(ms: Int, endPoint: Double, easeAmount: Float, direction: Direction?) : super(
        ms,
        endPoint,
        direction!!,
    ) {
        this.easeAmount = easeAmount
        this.reset()
    }

    override fun correctOutput(): Boolean = true

    protected override fun getEquation(x: Double): Double {
        val x1 = x / getDuration()
        val shrink = easeAmount + 1
        return max(0.0, 1 + shrink * (x1 - 1).pow(3.0) + easeAmount * (x1 - 1).pow(2.0))
    }
}
