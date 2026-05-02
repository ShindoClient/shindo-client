@file:JvmName("AnimationExtensions")

package me.miki.extensions.ui.animation

import me.miki.shindo.ui.animation.v1.Animation
import me.miki.shindo.ui.animation.v1.Direction
import me.miki.shindo.ui.animation.v1.TimedAnimation
import me.miki.shindo.ui.animation.v1.value.SimpleAnimation

/**
 * Extension helpers for [Animation] timelines.
 */
/** True when direction is forwards. */
fun Animation.isForwards(): Boolean = direction == Direction.FORWARDS

/** True when direction is backwards. */
fun Animation.isBackwards(): Boolean = direction == Direction.BACKWARDS

/**
 * Toggles the direction while preserving elapsed time.
 */
fun Animation.toggle() {
    changeDirection()
}

/**
 * Executes [block] once this animation is done in the given [dir].
 * Caller should guard repeated invocation externally if polled each frame.
 */
inline fun Animation.onDone(dir: Direction, block: () -> Unit) {
    if (isDone(dir)) block()
}

/**
 * Resets and points forwards.
 */
fun Animation.resetForwards() {
    reset()
    setDirection(Direction.FORWARDS)
}

/**
 * Resets and points backwards.
 */
fun Animation.resetBackwards() {
    reset()
    setDirection(Direction.BACKWARDS)
}

/**
 * Marks whether a timed animation is currently running (not finished).
 */
fun TimedAnimation.isRunning(): Boolean = !isDone()

val SimpleAnimation.currentValue: Float
    get() = value

var SimpleAnimation.animation: Float
    get() = value
    set(target) = setAnimation(target)
