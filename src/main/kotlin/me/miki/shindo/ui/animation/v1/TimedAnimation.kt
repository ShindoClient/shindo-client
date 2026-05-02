package me.miki.shindo.ui.animation.v1

open class TimedAnimation(
    ms: Int,
    endPoint: Double,
    private val equation: (elapsed: Double, duration: Int) -> Double
) : Animation(ms, endPoint) {

    init {
        reset()
    }

    override fun getEquation(x: Double): Double = equation(x, duration)
}
