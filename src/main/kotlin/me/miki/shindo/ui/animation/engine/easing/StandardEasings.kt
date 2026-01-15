package me.miki.shindo.ui.animation.engine.easing

import kotlin.math.*

/**
 * Funções de easing padrão (Linear, Quad, Cubic, Quart, Quint).
 * Baseadas no sistema antigo de easings.
 */

object StandardEasings {
    /**
     * Linear: sem easing, progressão constante.
     */
    val LINEAR: EasingFunction = { t -> t }

    /**
     * Quad: t²
     */
    val EASE_IN_QUAD: EasingFunction = { t -> t * t }
    val EASE_OUT_QUAD: EasingFunction = { t -> t * (2f - t) }
    val EASE_IN_OUT_QUAD: EasingFunction = { t ->
        val t2 = t * 2f
        if (t2 < 1f) {
            0.5f * t2 * t2
        } else {
            val t3 = t2 - 1f
            -0.5f * (t3 * (t3 - 2f) - 1f)
        }
    }

    /**
     * Cubic: t³
     */
    val EASE_IN_CUBIC: EasingFunction = { t -> t * t * t }
    val EASE_OUT_CUBIC: EasingFunction = { t ->
        val t1 = t - 1f
        t1 * t1 * t1 + 1f
    }
    val EASE_IN_OUT_CUBIC: EasingFunction = { t ->
        val t2 = t * 2f
        if (t2 < 1f) {
            0.5f * t2 * t2 * t2
        } else {
            val t3 = t2 - 2f
            0.5f * (t3 * t3 * t3 + 2f)
        }
    }

    /**
     * Quart: t⁴
     */
    val EASE_IN_QUART: EasingFunction = { t -> t * t * t * t }
    val EASE_OUT_QUART: EasingFunction = { t ->
        val t1 = t - 1f
        1f - t1 * t1 * t1 * t1
    }
    val EASE_IN_OUT_QUART: EasingFunction = { t ->
        val t2 = t * 2f
        if (t2 < 1f) {
            0.5f * t2 * t2 * t2 * t2
        } else {
            val t3 = t2 - 2f
            -0.5f * (t3 * t3 * t3 * t3 - 2f)
        }
    }

    /**
     * Quint: t⁵
     */
    val EASE_IN_QUINT: EasingFunction = { t -> t * t * t * t * t }
    val EASE_OUT_QUINT: EasingFunction = { t ->
        val t1 = t - 1f
        t1 * t1 * t1 * t1 * t1 + 1f
    }
    val EASE_IN_OUT_QUINT: EasingFunction = { t ->
        val t2 = t * 2f
        if (t2 < 1f) {
            0.5f * t2 * t2 * t2 * t2 * t2
        } else {
            val t3 = t2 - 2f
            0.5f * (t3 * t3 * t3 * t3 * t3 + 2f)
        }
    }
}
