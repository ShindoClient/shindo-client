package me.miki.shindo.ui.animation.engine

import me.miki.shindo.ui.animation.engine.easing.EasingFunction
import kotlin.math.min

abstract class AnimatedValue<T>(
    initial: T,
    private val controller: AnimationController? = AnimationController.global
) : Animatable {

    var value: T = initial
        protected set

    protected var start: T = initial
    protected var target: T = initial
    protected var elapsedMs: Long = 0
    protected var durationMs: Long = 0
    protected var easing: Easing = Easing.EASE_OUT

    override var isRunning: Boolean = false
        protected set

    fun snapTo(value: T) {
        this.value = value
        this.start = value
        this.target = value
        this.elapsedMs = 0
        this.durationMs = 0
        this.isRunning = false
    }

    fun animateTo(target: T, durationMs: Long = 250, easing: Easing = Easing.EASE_OUT): AnimatedValue<T> {
        this.start = value
        this.target = target
        this.elapsedMs = 0
        this.durationMs = durationMs
        this.easing = easing
        this.isRunning = true

        if (!GlobalAnimationSettings.enabled) {
            snapTo(target)
            return this
        }

        val scaledDuration = GlobalAnimationSettings.scaleDuration(durationMs)
        if (scaledDuration <= 0) {
            snapTo(target)
            return this
        }

        controller?.add(this)
        return this
    }

    /**
     * Anima para um valor usando uma EasingFunction diretamente.
     * Útil para usar easings customizados do sistema antigo.
     */
    fun animateTo(target: T, durationMs: Long = 250, easingFunction: EasingFunction): AnimatedValue<T> {
        return animateTo(target, durationMs, Easing.from(easingFunction))
    }

    override fun update(deltaMs: Long) {
        if (!isRunning) {
            return
        }

        if (!GlobalAnimationSettings.enabled) {
            snapTo(target)
            return
        }

        val scaledDuration = GlobalAnimationSettings.scaleDuration(durationMs)
        if (scaledDuration <= 0) {
            snapTo(target)
            return
        }

        elapsedMs += deltaMs
        val progress = min(1f, elapsedMs.toFloat() / scaledDuration.toFloat())
        val eased = easing.apply(progress)
        value = lerp(start, target, eased)

        if (progress >= 1f) {
            isRunning = false
        }
    }

    protected abstract fun lerp(from: T, to: T, t: Float): T
}
