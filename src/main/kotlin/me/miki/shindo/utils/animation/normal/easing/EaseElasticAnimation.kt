package me.miki.shindo.utils.animation.normal.easing

import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class EaseElasticAnimation : Animation {

    private val easeAmount: Float
    private val smooth: Float
    private val reallyElastic: Boolean

    constructor(ms: Int, endPoint: Double, elasticity: Float, smooth: Float, moreElasticity: Boolean) : super(ms, endPoint) {
        this.easeAmount = elasticity
        this.smooth = smooth
        this.reallyElastic = moreElasticity
        this.reset()
    }

    constructor(ms: Int, endPoint: Double, elasticity: Float, smooth: Float, moreElasticity: Boolean, direction: Direction) : super(ms, endPoint, direction) {
        this.easeAmount = elasticity
        this.smooth = smooth
        this.reallyElastic = moreElasticity
        this.reset()
    }

    override fun getEquation(x: Double): Double {
        val x1 = (x / duration).pow(smooth)
        val elasticity = easeAmount * 0.1f
        return 2.0.pow(-10 * if (reallyElastic) sqrt(x1) else x1) * sin((x1 - elasticity / 4) * (2 * Math.PI / elasticity)) + 1
    }
}
