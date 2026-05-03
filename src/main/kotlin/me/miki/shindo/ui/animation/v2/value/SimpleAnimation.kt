package me.miki.shindo.ui.animation.v2.value

import me.miki.shindo.ui.animation.v2.core.GlobalAnimationSettings
import kotlin.math.abs

open class SimpleAnimation(initialValue: Float = 0f) {

    @JvmField var value: Float = initialValue

    private var lastMs: Long = System.currentTimeMillis()

    fun toward(target: Float, speed: Double = 16.0) {
        if (!GlobalAnimationSettings.enabled) {
            value = target
            lastMs = System.currentTimeMillis()
            return
        }

        val now = System.currentTimeMillis()
        val delta = now - lastMs
        lastMs = now

        val clampedSpeed = speed.coerceIn(0.0, 28.0)
        val step = if (clampedSpeed != 0.0)
            abs(target - value) * 0.35f / (10.0 / clampedSpeed)
        else 0.0

        value = AnimationUtils.step(target, value, step, delta)
    }

    fun toward(target: Float, speed: Int) = toward(target, speed.toDouble())

    fun snap(target: Float) {
        value = target
        lastMs = System.currentTimeMillis()
    }
}
