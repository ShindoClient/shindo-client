package me.miki.shindo.ui.animation.engine.easing

/**
 * Arquivo central que exporta todos os easings disponíveis.
 * Use este arquivo para importar easings de forma organizada.
 * 
 * Exemplo de uso:
 * ```kotlin
 * import me.miki.shindo.ui.animation.engine.easing.Easings
 * 
 * val easing = Easings.EASE_OUT_CUBIC
 * ```
 */
object Easings {
    // Standard easings
    val LINEAR = StandardEasings.LINEAR
    val EASE_IN = StandardEasings.EASE_IN_QUAD
    val EASE_OUT = StandardEasings.EASE_OUT_QUAD
    val EASE_IN_OUT = StandardEasings.EASE_IN_OUT_QUAD

    // Quad
    val EASE_IN_QUAD = StandardEasings.EASE_IN_QUAD
    val EASE_OUT_QUAD = StandardEasings.EASE_OUT_QUAD
    val EASE_IN_OUT_QUAD = StandardEasings.EASE_IN_OUT_QUAD

    // Cubic
    val EASE_IN_CUBIC = StandardEasings.EASE_IN_CUBIC
    val EASE_OUT_CUBIC = StandardEasings.EASE_OUT_CUBIC
    val EASE_IN_OUT_CUBIC = StandardEasings.EASE_IN_OUT_CUBIC

    // Quart
    val EASE_IN_QUART = StandardEasings.EASE_IN_QUART
    val EASE_OUT_QUART = StandardEasings.EASE_OUT_QUART
    val EASE_IN_OUT_QUART = StandardEasings.EASE_IN_OUT_QUART

    // Quint
    val EASE_IN_QUINT = StandardEasings.EASE_IN_QUINT
    val EASE_OUT_QUINT = StandardEasings.EASE_OUT_QUINT
    val EASE_IN_OUT_QUINT = StandardEasings.EASE_IN_OUT_QUINT

    // Circular
    val EASE_IN_CIRC = CircularEasings.EASE_IN_CIRC
    val EASE_OUT_CIRC = CircularEasings.EASE_OUT_CIRC
    val EASE_IN_OUT_CIRC = CircularEasings.EASE_IN_OUT_CIRC

    // Exponential
    val EASE_IN_EXPO = ExponentialEasings.EASE_IN_EXPO
    val EASE_OUT_EXPO = ExponentialEasings.EASE_OUT_EXPO
    val EASE_IN_OUT_EXPO = ExponentialEasings.EASE_IN_OUT_EXPO

    // Sine
    val EASE_IN_SINE = SineEasings.EASE_IN_SINE
    val EASE_OUT_SINE = SineEasings.EASE_OUT_SINE
    val EASE_IN_OUT_SINE = SineEasings.EASE_IN_OUT_SINE

    // Back
    val EASE_IN_BACK = BackEasings.EASE_IN_BACK
    val EASE_OUT_BACK = BackEasings.EASE_OUT_BACK
    val EASE_IN_OUT_BACK = BackEasings.EASE_IN_OUT_BACK

    // Elastic
    val EASE_IN_ELASTIC = ElasticEasings.EASE_IN_ELASTIC
    val EASE_OUT_ELASTIC = ElasticEasings.EASE_OUT_ELASTIC
    val EASE_IN_OUT_ELASTIC = ElasticEasings.EASE_IN_OUT_ELASTIC

    // Smooth Step
    val SMOOTH_STEP = SmoothStepEasing.SMOOTH_STEP

    // Back com parâmetros customizados
    fun easeInBack(easeAmount: Float = 1.70158f) = BackEasings.easeInBack(easeAmount)
    fun easeOutBack(easeAmount: Float = 1.70158f) = BackEasings.easeOutBack(easeAmount)
    fun easeInOutBack(easeAmount: Float = 1.70158f) = BackEasings.easeInOutBack(easeAmount)

    // Elastic com parâmetros customizados
    fun easeInElastic(elasticity: Float = 0.3f, smooth: Float = 0.5f, reallyElastic: Boolean = false) =
        ElasticEasings.easeInElastic(elasticity, smooth, reallyElastic)
    fun easeOutElastic(elasticity: Float = 0.3f, smooth: Float = 0.5f, reallyElastic: Boolean = false) =
        ElasticEasings.easeOutElastic(elasticity, smooth, reallyElastic)
    fun easeInOutElastic(elasticity: Float = 0.3f, smooth: Float = 0.5f, reallyElastic: Boolean = false) =
        ElasticEasings.easeInOutElastic(elasticity, smooth, reallyElastic)
}
