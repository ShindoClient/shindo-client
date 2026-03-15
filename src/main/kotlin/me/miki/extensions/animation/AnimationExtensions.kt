@file:JvmName("AnimationExtensions")

package me.miki.extensions.animation

import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.TimedAnimation

/**
 * Extension helpers for [Animation] timelines.
 */
fun Animation.isForwards(): Boolean = direction == Direction.FORWARDS

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
