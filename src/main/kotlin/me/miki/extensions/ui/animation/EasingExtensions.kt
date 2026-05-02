@file:JvmName("EasingExtensions")

package me.miki.extensions.ui.animation

import me.miki.shindo.ui.animation.v1.EasingFunctions

/**
 * Progress (0.0..1.0) easing helpers on primitives.
 */
fun Float.easeInQuad(): Float = EasingFunctions.inQuad(this.toDouble(), 1).toFloat()
fun Float.easeOutQuad(): Float = EasingFunctions.outQuad(this.toDouble(), 1).toFloat()
fun Float.easeInOutQuad(): Float = EasingFunctions.inOutQuad(this.toDouble(), 1).toFloat()
fun Float.easeInCubic(): Float = EasingFunctions.inCubic(this.toDouble(), 1).toFloat()
fun Float.easeOutCubic(): Float = EasingFunctions.outCubic(this.toDouble(), 1).toFloat()
fun Float.easeInOutCubic(): Float = EasingFunctions.inOutCubic(this.toDouble(), 1).toFloat()
fun Float.easeInOutSine(): Float = EasingFunctions.inOutSine(this.toDouble(), 1).toFloat()
fun Float.easeOutExpo(): Float = EasingFunctions.outExpo(this.toDouble(), 1).toFloat()
fun Float.easeInBack(easeAmount: Float = 1.7f): Float = EasingFunctions.backIn(this.toDouble(), 1, easeAmount).toFloat()
fun Float.easeOutBack(easeAmount: Float = 1.7f): Float = EasingFunctions.outBack(this.toDouble(), 1, easeAmount.toDouble()).toFloat()
fun Float.easeInOutBack(easeAmount: Float = 1.7f): Float = EasingFunctions.inOutBack(this.toDouble(), 1, easeAmount.toDouble()).toFloat()

fun Double.easeInQuad(): Double = EasingFunctions.inQuad(this, 1)
fun Double.easeOutQuad(): Double = EasingFunctions.outQuad(this, 1)
fun Double.easeInOutQuad(): Double = EasingFunctions.inOutQuad(this, 1)
fun Double.easeInCubic(): Double = EasingFunctions.inCubic(this, 1)
fun Double.easeOutCubic(): Double = EasingFunctions.outCubic(this, 1)
fun Double.easeInOutCubic(): Double = EasingFunctions.inOutCubic(this, 1)
fun Double.easeInOutSine(): Double = EasingFunctions.inOutSine(this, 1)
fun Double.easeOutExpo(): Double = EasingFunctions.outExpo(this, 1)
fun Double.easeInBack(easeAmount: Float = 1.7f): Double = EasingFunctions.backIn(this, 1, easeAmount)
fun Double.easeOutBack(easeAmount: Float = 1.7f): Double = EasingFunctions.outBack(this, 1, easeAmount.toDouble())
fun Double.easeInOutBack(easeAmount: Float = 1.7f): Double = EasingFunctions.inOutBack(this, 1, easeAmount.toDouble())
