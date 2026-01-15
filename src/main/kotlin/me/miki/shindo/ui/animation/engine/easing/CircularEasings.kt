package me.miki.shindo.ui.animation.engine.easing

import kotlin.math.*

/**
 * Funções de easing circulares (Circ).
 * Baseadas no sistema antigo EaseInCirc, EaseOutCirc, EaseInOutCirc.
 */

object CircularEasings {
    /**
     * Ease In Circular: começa devagar e acelera.
     */
    val EASE_IN_CIRC: EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else 1f - sqrt(1f - t * t)
    }

    /**
     * Ease Out Circular: começa rápido e desacelera.
     */
    val EASE_OUT_CIRC: EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else {
            val t1 = t - 1f
            sqrt(1f - t1 * t1)
        }
    }

    /**
     * Ease In Out Circular: combina ease in e ease out.
     */
    val EASE_IN_OUT_CIRC: EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else {
            val t2 = t * 2f
            if (t2 < 1f) {
                val t3 = t2
                -0.5f * (sqrt(1f - t3 * t3) - 1f)
            } else {
                val t3 = t2 - 2f
                0.5f * (sqrt(1f - t3 * t3) + 1f)
            }
        }
    }
}
