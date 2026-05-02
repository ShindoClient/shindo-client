@file:JvmName("TimelineExtensions")

package me.miki.extensions.ui.animation

import me.miki.shindo.ui.animation.v1.Animation
import me.miki.shindo.ui.animation.v1.AnimationComponent
import me.miki.shindo.ui.animation.v1.Direction
import me.miki.shindo.utils.TimerUtils
import java.util.WeakHashMap

private data class TimelineState(
    val delayTimer: TimerUtils = TimerUtils(),
    var delayMs: Long = 0,
    var started: Boolean = false,
    var repeatMode: RepeatMode = RepeatMode.None,
    var remainingRepeats: Int = 0,
    var next: Animation? = null
)

private enum class RepeatMode { None, Forever, Counted }

// One WeakHashMap entry per animation; allocated on first use, then reused across frames.
private val timelineStore = WeakHashMap<Animation, TimelineState>()

private fun state(anim: Animation): TimelineState =
    timelineStore.getOrPut(anim) { TimelineState() }

/**
 * Delays the start of this animation by [delayMs] using [TimerUtils].
 * Call this each frame before reading values; returns true once started.
 */
fun Animation.delayedStart(delayMs: Long): Boolean {
    val s = state(this)
    if (!s.started) {
        s.delayMs = delayMs
        if (s.delayTimer.delay(delayMs.toFloat(), true)) {
            s.started = true
            reset()
        }
    }
    return s.started
}

/**
 * Toggles direction whenever the animation finishes, endlessly.
 */
fun Animation.repeatForever() {
    val s = state(this)
    s.repeatMode = RepeatMode.Forever
}

/**
 * Repeats this animation [times] (forward/backward toggling) then stops.
 */
fun Animation.repeatCount(times: Int) {
    val s = state(this)
    s.repeatMode = RepeatMode.Counted
    s.remainingRepeats = times
}

/**
 * Chains [next] animation to start when this one finishes forwards.
 */
fun Animation.then(next: Animation) {
    val s = state(this)
    s.next = next
}

/**
 * Should be called once per frame to drive repeat/chain logic without extra allocations.
 */
fun Animation.tickTimeline() {
    val s = timelineStore[this] ?: return

    if (isDone()) {
        when (s.repeatMode) {
            RepeatMode.Forever -> {
                changeDirection()
                reset()
            }
            RepeatMode.Counted -> {
                if (s.remainingRepeats > 0) {
                    s.remainingRepeats--
                    changeDirection()
                    reset()
                }
            }
            RepeatMode.None -> { /* no-op */ }
        }
        if (isDone() && s.repeatMode == RepeatMode.None) {
            s.next?.let { nextAnim ->
                nextAnim.reset()
                nextAnim.setDirection(Direction.FORWARDS)
            }
        }
    }
}

/**
 * Drives each timeline contained in [AnimationComponent] in the same polling pass.
 * This adapts grouped animations to existing timeline utilities without touching their implementations.
 */
fun AnimationComponent.tickTimelines() {
    forEachTimeline { it.tickTimeline() }
}
