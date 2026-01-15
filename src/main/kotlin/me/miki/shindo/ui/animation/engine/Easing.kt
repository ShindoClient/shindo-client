package me.miki.shindo.ui.animation.engine

import me.miki.shindo.ui.animation.engine.easing.EasingFunction
import me.miki.shindo.ui.animation.engine.easing.Easings

/**
 * Classe sealed para easings usada no animation engine.
 * Agora usa o sistema de easings completo do sistema antigo.
 * 
 * Para compatibilidade, mantém os easings básicos como objetos.
 * Para easings avançados, use diretamente as funções de easing.
 */
sealed class Easing {
    abstract fun apply(t: Float): Float

    /**
     * Cria um Easing a partir de uma EasingFunction.
     */
    class Custom(private val function: EasingFunction) : Easing() {
        override fun apply(t: Float): Float = function(t)
    }

    object LINEAR : Easing() {
        override fun apply(t: Float): Float = Easings.LINEAR(t)
    }

    object EASE_IN : Easing() {
        override fun apply(t: Float): Float = Easings.EASE_IN(t)
    }

    object EASE_OUT : Easing() {
        override fun apply(t: Float): Float = Easings.EASE_OUT(t)
    }

    object EASE_IN_OUT : Easing() {
        override fun apply(t: Float): Float = Easings.EASE_IN_OUT(t)
    }

    object EXPONENTIAL : Easing() {
        override fun apply(t: Float): Float = Easings.EASE_OUT_EXPO(t)
    }

    object SMOOTH_STEP : Easing() {
        override fun apply(t: Float): Float = Easings.SMOOTH_STEP(t)
    }

    companion object {
        /**
         * Cria um Easing customizado a partir de uma EasingFunction.
         */
        fun from(function: EasingFunction): Easing = Custom(function)
    }
}
