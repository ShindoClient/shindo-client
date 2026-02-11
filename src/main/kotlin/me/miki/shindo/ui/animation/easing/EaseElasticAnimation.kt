package me.miki.shindo.ui.animation.easing

import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.EasingFunctions

class EaseElasticAnimation : Animation {

    private val easeAmount: Float
    private val smooth: Float
    private val reallyElastic: Boolean

    constructor(
        ms: Int,
        endPoint: Double,
        elasticity: Float,
        smooth: Float,
        moreElasticity: Boolean
    ) : super(ms, endPoint) {
        easeAmount = elasticity
        this.smooth = smooth
        reallyElastic = moreElasticity
        reset()
    }

    constructor(
        ms: Int,
        endPoint: Double,
        elasticity: Float,
        smooth: Float,
        moreElasticity: Boolean,
        direction: Direction
    ) : super(ms, endPoint, direction) {
        easeAmount = elasticity
        this.smooth = smooth
        reallyElastic = moreElasticity
        reset()
    }

    override fun getEquation(x: Double): Double =
        EasingFunctions.elastic(x, duration, easeAmount, smooth, reallyElastic)
}
