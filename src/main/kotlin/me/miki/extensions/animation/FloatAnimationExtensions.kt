@file:JvmName("FloatAnimationExtensions")

package me.miki.extensions.animation

import me.miki.shindo.ui.animation.value.FloatAnimation

/**
 * Extension helpers for [FloatAnimation].
 */
fun FloatAnimation.lerpTo(target: Float): Float {
    val current = getValue()
    val progress = getValueFloat()
    return current + (target - current) * progress
}

fun FloatAnimation.snapTo(value: Float) {
    // Fast-forward to end or start based on proximity.
    val current = getValue()
    val directionToEnd = value >= current
    setValue(if (directionToEnd) 1.0 else 0.0)
}

inline fun FloatAnimation.onComplete(block: () -> Unit) {
    if (isDone()) block()
}
