package me.miki.shindo.ui.animation.value

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
) {
    private val animX = FloatAnimation(durationMs, startX, endX, easing)
    private val animY = FloatAnimation(durationMs, startY, endY, easing)

    fun getX(): Float = animX.getFloatValue()
    fun getY(): Float = animY.getFloatValue()

    fun isDoneX(): Boolean = animX.isDone()
    fun isDoneY(): Boolean = animY.isDone()

    fun snapTo(x: Float, y: Float) {
        animX.setValue(if (x >= getX()) 1.0 else 0.0)
        animY.setValue(if (y >= getY()) 1.0 else 0.0)
    }

    /**
     * True when both component animations have completed.
     */
    fun isDone(): Boolean = animX.isDone() && animY.isDone()
}
