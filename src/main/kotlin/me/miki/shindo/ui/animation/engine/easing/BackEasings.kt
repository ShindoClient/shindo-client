package me.miki.shindo.ui.animation.engine.easing

import kotlin.math.*

/**
 * Funções de easing Back (com overshoot).
 * Baseadas no sistema antigo EaseBackIn.
 * 
 * @param easeAmount Quantidade de overshoot (padrão: 1.70158f, que é o valor padrão comum)
 */
object BackEasings {
    private const val DEFAULT_EASE_AMOUNT = 1.70158f

    /**
     * Ease In Back: começa com overshoot para trás.
     */
    fun easeInBack(easeAmount: Float = DEFAULT_EASE_AMOUNT): EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else {
            val shrink = easeAmount + 1f
            max(0f, 1f + shrink * (t - 1f).pow(3f) + easeAmount * (t - 1f).pow(2f))
        }
    }

    /**
     * Ease Out Back: termina com overshoot para frente.
     */
    fun easeOutBack(easeAmount: Float = DEFAULT_EASE_AMOUNT): EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else {
            val t1 = t - 1f
            val shrink = easeAmount + 1f
            val result = 1f + shrink * t1.pow(3f) + easeAmount * t1.pow(2f)
            result.coerceIn(0f, 1f)
        }
    }

    /**
     * Ease In Out Back: combina ease in e ease out com overshoot.
     */
    fun easeInOutBack(easeAmount: Float = DEFAULT_EASE_AMOUNT): EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else {
            val t2 = t * 2f
            val shrink = easeAmount * 1.525f
            if (t2 < 1f) {
                0.5f * (1f + shrink * (t2 - 1f).pow(3f) + (shrink + 1f) * (t2 - 1f).pow(2f))
            } else {
                val t3 = t2 - 2f
                0.5f * (1f + shrink * t3.pow(3f) + (shrink + 1f) * t3.pow(2f)) + 0.5f
            }
        }
    }

    /**
     * Versões com easeAmount padrão para uso direto.
     */
    val EASE_IN_BACK: EasingFunction = easeInBack()
    val EASE_OUT_BACK: EasingFunction = easeOutBack()
    val EASE_IN_OUT_BACK: EasingFunction = easeInOutBack()
}
