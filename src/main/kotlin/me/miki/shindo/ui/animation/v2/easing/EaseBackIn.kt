package me.miki.shindo.ui.animation.v2.easing

import me.miki.shindo.ui.animation.v1.Direction
import me.miki.shindo.ui.animation.v1.EasingFunctions
import me.miki.shindo.ui.animation.v1.TimedAnimation

class EaseBackIn(
    ms: Int,
    endPoint: Double,
    private val easeAmount: Float = 1.7f
) : TimedAnimation(ms, endPoint, { e, d -> EasingFunctions.backIn(e, d, easeAmount) }) {

    constructor(ms: Int, endPoint: Double, easeAmount: Float, direction: Direction) : this(ms, endPoint, easeAmount) {
        setDirection(direction)
    }

    override fun correctOutput(): Boolean = true
}
