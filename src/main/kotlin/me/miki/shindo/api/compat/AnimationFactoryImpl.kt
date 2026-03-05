package me.miki.shindo.api.compat

import me.miki.client_api.animation.EasingType
import me.miki.client_api.animation.IAnimation
import me.miki.client_api.animation.IAnimationFactory
import me.miki.client_api.animation.IColorAnimation
import me.miki.client_api.animation.ITimedAnimation
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.value.ColorAnimation
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.animation.curve.DecelerateAnimation
import me.miki.shindo.ui.animation.curve.SmoothStepAnimation
import me.miki.shindo.ui.animation.easing.EaseBackIn
import me.miki.shindo.ui.animation.easing.EaseElasticAnimation
import me.miki.shindo.ui.animation.easing.EaseInCirc
import me.miki.shindo.ui.animation.easing.EaseInCubic
import me.miki.shindo.ui.animation.easing.EaseInExpo
import me.miki.shindo.ui.animation.easing.EaseInOutCirc
import me.miki.shindo.ui.animation.easing.EaseInOutCubic
import me.miki.shindo.ui.animation.easing.EaseInOutExpo
import me.miki.shindo.ui.animation.easing.EaseInOutQuad
import me.miki.shindo.ui.animation.easing.EaseInOutQuart
import me.miki.shindo.ui.animation.easing.EaseInOutQuint
import me.miki.shindo.ui.animation.easing.EaseInOutSine
import me.miki.shindo.ui.animation.easing.EaseInQuad
import me.miki.shindo.ui.animation.easing.EaseInQuart
import me.miki.shindo.ui.animation.easing.EaseInQuint
import me.miki.shindo.ui.animation.easing.EaseInSine
import me.miki.shindo.ui.animation.easing.EaseLiner
import me.miki.shindo.ui.animation.easing.EaseOutCirc
import me.miki.shindo.ui.animation.easing.EaseOutCubic
import me.miki.shindo.ui.animation.easing.EaseOutExpo
import me.miki.shindo.ui.animation.easing.EaseOutQuad
import me.miki.shindo.ui.animation.easing.EaseOutQuart
import me.miki.shindo.ui.animation.easing.EaseOutQuint
import me.miki.shindo.ui.animation.easing.EaseOutSine

/**
 * Implementação de IAnimationFactory que delega ao client.
 */
class AnimationFactoryImpl : IAnimationFactory {

    override fun createAnimation(): IAnimation = AnimationAdapter.wrap(SimpleAnimation())

    override fun createAnimation(initialValue: Float): IAnimation = AnimationAdapter.wrap(SimpleAnimation(initialValue))

    override fun createSmoothStepAnimation(durationMs: Int, endPoint: Double): ITimedAnimation =
        AnimationAdapter.wrapTimed(SmoothStepAnimation(durationMs, endPoint))

    override fun createTimedAnimation(durationMs: Int, endPoint: Double, easing: EasingType): ITimedAnimation =
        AnimationAdapter.wrapTimed(createAnimationForEasing(durationMs, endPoint, easing))

    override fun createElasticAnimation(durationMs: Int, endPoint: Double, elasticity: Float, smooth: Float, reallyElastic: Boolean): ITimedAnimation =
        AnimationAdapter.wrapTimed(EaseElasticAnimation(durationMs, endPoint, elasticity, smooth, reallyElastic))

    override fun createBackInAnimation(durationMs: Int, endPoint: Double, easeAmount: Float): ITimedAnimation =
        AnimationAdapter.wrapTimed(EaseBackIn(durationMs, endPoint, easeAmount))

    override fun createColorAnimation(): IColorAnimation = AnimationAdapter.wrapColor(ColorAnimation())

    private fun createAnimationForEasing(durationMs: Int, endPoint: Double, easing: EasingType): Animation {
        return when (easing) {
            EasingType.LINEAR -> EaseLiner(durationMs, endPoint)
            EasingType.SMOOTH_STEP -> SmoothStepAnimation(durationMs, endPoint)
            EasingType.DECELERATE -> DecelerateAnimation(durationMs, endPoint)
            EasingType.IN_OUT_CIRC -> EaseInOutCirc(durationMs, endPoint)
            EasingType.BACK_IN -> EaseBackIn(durationMs, endPoint, 1.7f)
            EasingType.IN_QUAD -> EaseInQuad(durationMs, endPoint)
            EasingType.OUT_QUAD -> EaseOutQuad(durationMs, endPoint)
            EasingType.IN_OUT_QUAD -> EaseInOutQuad(durationMs, endPoint)
            EasingType.IN_CUBIC -> EaseInCubic(durationMs, endPoint)
            EasingType.OUT_CUBIC -> EaseOutCubic(durationMs, endPoint)
            EasingType.IN_OUT_CUBIC -> EaseInOutCubic(durationMs, endPoint)
            EasingType.IN_CIRC -> EaseInCirc(durationMs, endPoint)
            EasingType.OUT_CIRC -> EaseOutCirc(durationMs, endPoint)
            EasingType.IN_SINE -> EaseInSine(durationMs, endPoint)
            EasingType.OUT_SINE -> EaseOutSine(durationMs, endPoint)
            EasingType.IN_OUT_SINE -> EaseInOutSine(durationMs, endPoint)
            EasingType.IN_EXPO -> EaseInExpo(durationMs, endPoint)
            EasingType.OUT_EXPO -> EaseOutExpo(durationMs, endPoint)
            EasingType.IN_OUT_EXPO -> EaseInOutExpo(durationMs, endPoint)
            EasingType.IN_QUART -> EaseInQuart(durationMs, endPoint)
            EasingType.OUT_QUART -> EaseOutQuart(durationMs, endPoint)
            EasingType.IN_OUT_QUART -> EaseInOutQuart(durationMs, endPoint)
            EasingType.IN_QUINT -> EaseInQuint(durationMs, endPoint)
            EasingType.OUT_QUINT -> EaseOutQuint(durationMs, endPoint)
            EasingType.IN_OUT_QUINT -> EaseInOutQuint(durationMs, endPoint)
            EasingType.ELASTIC -> EaseElasticAnimation(durationMs, endPoint, 0.3f, 1f, false)
        }
    }
}
