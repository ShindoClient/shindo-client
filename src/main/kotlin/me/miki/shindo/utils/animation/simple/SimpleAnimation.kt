package me.miki.shindo.utils.animation.simple

import me.miki.shindo.utils.animation.simple.AnimationUtils.calculateCompensation
import kotlin.math.abs
import kotlin.system.*

class SimpleAnimation {

    var value: Float = 0.0F
    private var lastMS: Long = System.currentTimeMillis()

    constructor()

    constructor(value: Float) {
        this.value = value
        this.lastMS = System.currentTimeMillis()
    }

    fun setAnimation(value: Float, speed: Double) {
        val currentMS = System.currentTimeMillis()
        val delta = currentMS - this.lastMS
        this.lastMS = currentMS

        var speed = speed
        var deltaValue = 0.0

        if (speed > 28) {
            speed = 28.0
        }

        if (speed != 0.0) {
            deltaValue = abs(value - this.value) * 0.35f / (10.0 / speed)
        }

        this.value = calculateCompensation(value, this.value, deltaValue, delta).toFloat()
    }
}