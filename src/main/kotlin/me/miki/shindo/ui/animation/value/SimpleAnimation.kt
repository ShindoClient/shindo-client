package me.miki.shindo.ui.animation.value

import me.miki.shindo.ui.animation.GlobalAnimationSettings
import me.miki.shindo.ui.animation.value.AnimationUtils.calculateCompensation
import kotlin.math.abs

open class SimpleAnimation {

    @JvmField
    var value: Float = 0f

    private var lastMS: Long = System.currentTimeMillis()

    constructor()

    constructor(initialValue: Float) {
        value = initialValue
        lastMS = System.currentTimeMillis()
    }

    fun setAnimation(target: Float, speed: Double) {
        if (!GlobalAnimationSettings.enabled) {
            value = target
            lastMS = System.currentTimeMillis()
            return
        }
        val currentMS = System.currentTimeMillis()
        val delta = currentMS - lastMS
        lastMS = currentMS

        var speedClamped = speed
        if (speedClamped > 28) speedClamped = 28.0

        val deltaValue = if (speedClamped != 0.0) abs(target - value) * 0.35f / (10.0 / speedClamped) else 0.0
        value = calculateCompensation(target, value, deltaValue, delta)
    }

    fun setAnimation(target: Float, speed: Int) {
        setAnimation(target, speed.toDouble())
    }

    fun setAnimation(target: Float) {
        setAnimation(target, 16.0)
    }
}
