package me.miki.shindo.ui.animation.v2.value

import me.miki.shindo.ui.animation.v2.core.Animation
import me.miki.shindo.ui.animation.v2.core.Direction


class FloatAnimation(
    durationMs: Int,
    val startValue: Float,
    val endValue: Float,
    easing: (elapsed: Double, duration: Int) -> Double,
    direction: Direction = Direction.FORWARDS
) : Animation(durationMs, 1.0, easing, direction) {

    val current: Float
        get() = startValue + (endValue - startValue) * getValueFloat()
}
