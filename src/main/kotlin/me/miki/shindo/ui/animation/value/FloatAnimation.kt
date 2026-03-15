package me.miki.shindo.ui.animation.value

import me.miki.shindo.ui.animation.TimedAnimation

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
) : TimedAnimation(durationMs, 1.0, easing) {

    /**
     * Current value without allocating; respects global animation scaling via the base class.
     */
    fun getFloatValue(): Float {
        val progress = getValueFloat() // base outputs 0..1
        return (startValue + (endValue - startValue) * progress)
    }
}
