package me.miki.shindo.addon.api.animation

/**
 * Factory para criar animações. O client fornece implementação.
 */
interface IAnimationFactory {

    fun createAnimation(): IAnimation

    fun createAnimation(initialValue: Float): IAnimation

    /**
     * Cria animação com curva smooth step.
     * @param durationMs duração em milissegundos
     * @param endPoint valor final (0.0 a 1.0)
     */
    fun createSmoothStepAnimation(durationMs: Int, endPoint: Double = 1.0): ITimedAnimation

    /**
     * Cria animação timed com easing configurável.
     * @param durationMs duração em milissegundos
     * @param endPoint valor final (0.0 a 1.0)
     * @param easing tipo de curva de easing
     */
    fun createTimedAnimation(durationMs: Int, endPoint: Double = 1.0, easing: EasingType = EasingType.SMOOTH_STEP): ITimedAnimation

    /**
     * Cria animação elastic com parâmetros configuráveis.
     * @param elasticity intensidade da elasticidade
     * @param smooth suavidade
     * @param reallyElastic mais elástico
     */
    fun createElasticAnimation(durationMs: Int, endPoint: Double = 1.0, elasticity: Float = 0.3f, smooth: Float = 1f, reallyElastic: Boolean = false): ITimedAnimation

    /**
     * Cria animação back-in com parâmetro configurável.
     * @param easeAmount quanto maior, mais "overshoot" (ex: 1.8f, 2.0f)
     */
    fun createBackInAnimation(durationMs: Int, endPoint: Double = 1.0, easeAmount: Float = 1.7f): ITimedAnimation

    fun createColorAnimation(): IColorAnimation
}
