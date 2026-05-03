package me.miki.shindo.ui.animation.v2.value

import kotlin.math.abs


internal object AnimationUtils {

    fun step(target: Float, current: Float, speed: Double, delta: Long): Float {
        val diff = current - target
        val step = delta * (speed / 50.0)

        return when {
            diff > speed  -> if (current - step > target) (current - step).toFloat() else target
            diff < -speed -> if (current + step < target) (current + step).toFloat() else target
            else          -> if (abs(current - target) < 0.03f) target else current
        }
    }
}
