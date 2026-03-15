package me.miki.shindo.ui.animation.value

import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.AnimationComponent

/**
 * Animates a 2D vector using two [FloatAnimation] instances.
 *
 * Uses independent x/y timelines but shares the same duration/easing inputs.
 * No per-frame allocations; callers read components via getters.
 */
class Vector2fAnimation(
    durationMs: Int,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    easing: (elapsed: Double, duration: Int) -> Double
): AnimationComponent {
    private val animX = FloatAnimation(durationMs, startX, endX, easing)
    private val animY = FloatAnimation(durationMs, startY, endY, easing)

    /** Current X component without allocations. */
    fun getX(): Float = animX.getFloatValue()

    /** Current Y component without allocations. */
    fun getY(): Float = animY.getFloatValue()

    /** True when the X timeline has finished. */
    fun isDoneX(): Boolean = animX.isDone()

    /** True when the Y timeline has finished. */
    fun isDoneY(): Boolean = animY.isDone()

    /**
     * Jumps to the provided coordinates; uses the base snap by forcing progress to 0 or 1.
     */
    fun snapTo(x: Float, y: Float) {
        animX.setValue(if (x >= getX()) 1.0 else 0.0)
        animY.setValue(if (y >= getY()) 1.0 else 0.0)
    }

    /**
     * True when both component animations have completed.
     */
    fun isDone(): Boolean = animX.isDone() && animY.isDone()

    override fun forEachTimeline(action: (Animation) -> Unit) {
        action(animX)
        action(animY)
    }
}
