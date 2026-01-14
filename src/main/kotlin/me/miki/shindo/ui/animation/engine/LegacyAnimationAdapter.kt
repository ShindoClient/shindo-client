package me.miki.shindo.ui.animation.engine

import kotlin.math.max

class LegacyAnimationAdapter(
    initial: Float = 0f,
    private val controller: AnimationController? = AnimationController.global
) {
    private val animated = AnimatedFloat(initial, controller)

    var value: Float
        get() = animated.value
        set(v) {
            animated.snapTo(v)
        }

    fun setAnimation(target: Float, speed: Double) {
        val safeSpeed = max(0.1, speed)
        val durationMs = (1000.0 / safeSpeed).toLong().coerceAtLeast(1L)
        animated.animateTo(target, durationMs, Easing.EASE_OUT)
    }
}
