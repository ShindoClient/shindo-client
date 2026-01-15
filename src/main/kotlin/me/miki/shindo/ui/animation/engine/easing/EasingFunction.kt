package me.miki.shindo.ui.animation.engine.easing

/**
 * Função de easing que recebe um valor t normalizado (0.0 a 1.0) e retorna o valor eased.
 * Todas as funções devem retornar valores no range [0.0, 1.0] quando t está em [0.0, 1.0].
 */
typealias EasingFunction = (Float) -> Float

/**
 * Utilitários para criar e combinar funções de easing.
 */
object EasingUtils {
    /**
     * Clampa t entre 0.0 e 1.0.
     */
    fun clamp(t: Float): Float = t.coerceIn(0f, 1f)

    /**
     * Combina duas funções de easing (composição).
     */
    fun compose(first: EasingFunction, second: EasingFunction): EasingFunction {
        return { t -> second(first(clamp(t))) }
    }

    /**
     * Inverte uma função de easing (reverse).
     */
    fun reverse(easing: EasingFunction): EasingFunction {
        return { t -> 1f - easing(1f - clamp(t)) }
    }

    /**
     * Cria uma função de easing que é uma combinação de ease-in e ease-out.
     */
    fun inOut(easeIn: EasingFunction, easeOut: EasingFunction): EasingFunction {
        return { t ->
            val clamped = clamp(t)
            if (clamped < 0.5f) {
                0.5f * easeIn(clamped * 2f)
            } else {
                0.5f + 0.5f * easeOut((clamped - 0.5f) * 2f)
            }
        }
    }
}
