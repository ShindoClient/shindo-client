package me.miki.shindo.ui.animation.v2.easing

import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class EaseElasticAnimation : Animation {
    var easeAmount: Float
    var smooth: Float
    var reallyElastic: Boolean

    constructor(ms: Int, endPoint: Double, elasticity: Float, smooth: Float, moreElasticity: Boolean) : super(
        ms,
        endPoint
    ) {
        this.easeAmount = elasticity
        this.smooth = smooth
        this.reallyElastic = moreElasticity
        this.reset()
    }

    constructor(
        ms: Int,
        endPoint: Double,
        elasticity: Float,
        smooth: Float,
        moreElasticity: Boolean,
        direction: Direction?
    ) : super(ms, endPoint, direction!!) {
        this.easeAmount = elasticity
        this.smooth = smooth
        this.reallyElastic = moreElasticity
        this.reset()
    }

    protected override fun getEquation(x: Double): Double {
        val x1 = (x / getDuration()).pow(smooth.toDouble())
        val elasticity = (easeAmount * .1f).toDouble()

        return 2.0.pow(-10 * (if (reallyElastic) sqrt(x1) else x1)) * sin((x1 - (elasticity / 4)) * ((2 * Math.PI) / elasticity)) + 1
    }
}