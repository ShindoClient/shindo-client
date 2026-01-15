package me.miki.shindo.ui.animation.engine.easing

import kotlin.math.*

/**
 * Funções de easing exponenciais (Expo).
 * Baseadas no sistema antigo EaseInExpo, EaseOutExpo, EaseInOutExpo.
 */

object ExponentialEasings {
    /**
     * Ease In Exponential: começa muito devagar e acelera exponencialmente.
     */
    val EASE_IN_EXPO: EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else 2.0.pow(10f * (t - 1f)).toFloat()
    }

    /**
     * Ease Out Exponential: começa rápido e desacelera exponencialmente.
     */
    val EASE_OUT_EXPO: EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else 1f - 2.0.pow(-10f * t).toFloat()
    }

    /**
     * Ease In Out Exponential: combina ease in e ease out exponenciais.
     */
    val EASE_IN_OUT_EXPO: EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else {
            val t2 = t * 2f
            if (t2 < 1f) {
                0.5f * 2.0.pow(10f * (t2 - 1f)).toFloat()
            } else {
                0.5f * (2f - 2.0.pow(-10f * (t2 - 1f)).toFloat())
            }
        }
    }
}
