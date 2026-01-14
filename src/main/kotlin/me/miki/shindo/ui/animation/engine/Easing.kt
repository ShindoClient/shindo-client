package me.miki.shindo.ui.animation.engine

import kotlin.math.pow

sealed class Easing {
    abstract fun apply(t: Float): Float

    object LINEAR : Easing() {
        override fun apply(t: Float): Float = t
    }

    object EASE_IN : Easing() {
        override fun apply(t: Float): Float = t * t
    }

    object EASE_OUT : Easing() {
        override fun apply(t: Float): Float = t * (2f - t)
    }

    object EASE_IN_OUT : Easing() {
        override fun apply(t: Float): Float {
            return if (t < 0.5f) {
                2f * t * t
            } else {
                -1f + (4f - 2f * t) * t
            }
        }
    }

    object EXPONENTIAL : Easing() {
        override fun apply(t: Float): Float {
            if (t <= 0f) return 0f
            if (t >= 1f) return 1f
            return (1.0 - 2.0.pow((-10f * t).toDouble())).toFloat()
        }
    }

    object SMOOTH_STEP : Easing() {
        override fun apply(t: Float): Float = t * t * (3f - 2f * t)
    }
}
