package me.miki.shindo.ui.animation.engine.easing

import kotlin.math.*

/**
 * Funções de easing Elastic (com bounce/elasticidade).
 * Baseadas no sistema antigo EaseElasticAnimation.
 * 
 * @param elasticity Elasticidade (padrão: 0.3f)
 * @param smooth Suavidade (padrão: 0.5f)
 * @param reallyElastic Se deve usar elasticidade mais pronunciada (padrão: false)
 */
object ElasticEasings {
    private const val DEFAULT_ELASTICITY = 0.3f
    private const val DEFAULT_SMOOTH = 0.5f

    /**
     * Ease In Elastic: começa com bounce elástico.
     */
    fun easeInElastic(
        elasticity: Float = DEFAULT_ELASTICITY,
        smooth: Float = DEFAULT_SMOOTH,
        reallyElastic: Boolean = false
    ): EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else {
            val x1 = t.pow(smooth)
            val elastic = elasticity * 0.1f
            val base = if (reallyElastic) sqrt(x1) else x1
            val result = (-2.0.pow(-10f * base).toFloat()) * sin((base - elastic / 4f) * (2f * PI.toFloat() / elastic)) + 1f
            result.coerceIn(0f, 1f)
        }
    }

    /**
     * Ease Out Elastic: termina com bounce elástico.
     */
    fun easeOutElastic(
        elasticity: Float = DEFAULT_ELASTICITY,
        smooth: Float = DEFAULT_SMOOTH,
        reallyElastic: Boolean = false
    ): EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else {
            val x1 = t.pow(smooth)
            val elastic = elasticity * 0.1f
            val base = if (reallyElastic) sqrt(x1) else x1
            val result = 2.0.pow(-10f * base).toFloat() * sin((base - elastic / 4f) * (2f * PI.toFloat() / elastic)) + 1f
            result.coerceIn(0f, 1f)
        }
    }

    /**
     * Ease In Out Elastic: combina ease in e ease out elásticos.
     */
    fun easeInOutElastic(
        elasticity: Float = DEFAULT_ELASTICITY,
        smooth: Float = DEFAULT_SMOOTH,
        reallyElastic: Boolean = false
    ): EasingFunction = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else {
            val t2 = t * 2f
            if (t2 < 1f) {
                0.5f * easeInElastic(elasticity, smooth, reallyElastic)(t2)
            } else {
                0.5f + 0.5f * easeOutElastic(elasticity, smooth, reallyElastic)(t2 - 1f)
            }
        }
    }

    /**
     * Versões com parâmetros padrão para uso direto.
     */
    val EASE_IN_ELASTIC: EasingFunction = easeInElastic()
    val EASE_OUT_ELASTIC: EasingFunction = easeOutElastic()
    val EASE_IN_OUT_ELASTIC: EasingFunction = easeInOutElastic()
}
