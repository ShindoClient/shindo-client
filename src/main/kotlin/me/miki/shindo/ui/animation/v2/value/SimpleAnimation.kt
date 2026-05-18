package me.miki.shindo.ui.animation.v2.value

import me.miki.shindo.ui.animation.v2.value.AnimationUtils.calculateCompensation
import kotlin.math.abs

class SimpleAnimation {
    private var value: Float
    private var lastMS: Long

    constructor() {
        this.value = 0.0f
        this.lastMS = System.currentTimeMillis()
    }

    constructor(value: Float) {
        this.value = value
        this.lastMS = System.currentTimeMillis()
    }

    fun setAnimation(
        value: Float,
        speed: Double,
    ) {
        var speed = speed
        val currentMS = System.currentTimeMillis()
        val delta = currentMS - this.lastMS
        this.lastMS = currentMS

        var deltaValue = 0.0

        if (speed > 28) {
            speed = 28.0
        }

        if (speed != 0.0) {
            deltaValue = abs(value - this.value) * 0.35f / (10.0 / speed)
        }

        this.value = calculateCompensation(value, this.value, deltaValue, delta)
    }

    fun setAnimation(target: Float) {
        setAnimation(target, 16.0)
    }

    fun getValue(): Float = value

    fun setValue(value: Float) {
        this.value = value
    }
}