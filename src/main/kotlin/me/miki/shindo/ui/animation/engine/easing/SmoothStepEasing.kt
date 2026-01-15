package me.miki.shindo.ui.animation.engine.easing

/**
 * Função de easing Smooth Step (Hermite interpolation).
 * Equivalente ao SMOOTH_STEP do sistema antigo.
 */
object SmoothStepEasing {
    /**
     * Smooth Step: interpolação suave de Hermite.
     */
    val SMOOTH_STEP: EasingFunction = { t ->
        t * t * (3f - 2f * t)
    }
}
