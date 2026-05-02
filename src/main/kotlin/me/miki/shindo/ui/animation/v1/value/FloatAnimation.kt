package me.miki.shindo.ui.animation.v1.value

import me.miki.shindo.ui.animation.v1.Animation
import me.miki.shindo.ui.animation.v1.AnimationComponent
import me.miki.shindo.ui.animation.v1.TimedAnimation

/**
 * Timeline-based float animation using [TimedAnimation] with a supplied easing function.
 *
 * @param durationMs Duration in milliseconds.
 * @param startValue Starting float value.
 * @param endValue Target float value.
 * @param easing Easing equation receiving elapsed and duration.
 */
class FloatAnimation(
    durationMs: Int,
    private val startValue: Float,
    private val endValue: Float,
    easing: (elapsed: Double, duration: Int) -> Double
) : TimedAnimation(durationMs, 1.0, easing), AnimationComponent {

    /**
     * Current value without allocating; respects global animation scaling via the base class.
     */
    fun getFloatValue(): Float {
        val progress = getValueFloat() // base outputs 0..1
        return (startValue + (endValue - startValue) * progress)
    }

    override fun forEachTimeline(action: (Animation) -> Unit) {
        action(this)
    }
}
