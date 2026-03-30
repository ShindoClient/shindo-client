@file:JvmName("FloatAnimationExtensions")

package me.miki.extensions.ui.animation

import me.miki.shindo.ui.animation.value.FloatAnimation

/**
 * Extension helpers for [FloatAnimation].
 */
/** Interpolates toward [target] using the current animation progress. */
fun FloatAnimation.lerpTo(target: Float): Float {
    val current = getFloatValue()
    val progress = getValueFloat()
    return current + (target - current) * progress
}

/** Snaps the animation to the nearest edge relative to [value]. */
fun FloatAnimation.snapTo(value: Float) {
    // Fast-forward to end or start based on proximity.
    val current = getFloatValue()
    val directionToEnd = value >= current
    setValue(if (directionToEnd) 1.0 else 0.0)
}

/** Executes [block] once the animation has completed. */
inline fun FloatAnimation.onComplete(block: () -> Unit) {
    if (isDone()) block()
}
