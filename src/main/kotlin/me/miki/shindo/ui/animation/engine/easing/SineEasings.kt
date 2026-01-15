package me.miki.shindo.ui.animation.engine.easing

import kotlin.math.*

/**
 * Funções de easing senoidais (Sine).
 * Baseadas no sistema antigo EaseInSine, EaseOutSine, EaseInOutSine.
 */

object SineEasings {
    /**
     * Ease In Sine: começa devagar com curva senoidal.
     */
    val EASE_IN_SINE: EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else 1f - cos(t * PI.toFloat() / 2f)
    }

    /**
     * Ease Out Sine: termina devagar com curva senoidal.
     */
    val EASE_OUT_SINE: EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else sin(t * PI.toFloat() / 2f)
    }

    /**
     * Ease In Out Sine: combina ease in e ease out senoidais.
     */
    val EASE_IN_OUT_SINE: EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else -0.5f * (cos(PI.toFloat() * t) - 1f)
    }
}
