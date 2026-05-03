package me.miki.shindo.ui.animation.v2.value

import me.miki.shindo.ui.animation.v2.core.Direction

class Vector2fAnimation(
    durationMs: Int,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    easing: (elapsed: Double, duration: Int) -> Double,
    direction: Direction = Direction.FORWARDS
) {
    private val animX = FloatAnimation(durationMs, startX, endX, easing, direction)
    private val animY = FloatAnimation(durationMs, startY, endY, easing, direction)

    val x: Float get() = animX.current
    val y: Float get() = animY.current

    val isDone: Boolean get() = animX.isDone() && animY.isDone()

    fun changeDirection() { animX.changeDirection(); animY.changeDirection() }
    fun setDirection(dir: Direction) { animX.setDirection(dir); animY.setDirection(dir) }
    fun reset() { animX.reset(); animY.reset() }

    fun snapTo(x: Float, y: Float) {
        animX.setProgress(if (x >= animX.current) 1.0 else 0.0)
        animY.setProgress(if (y >= animY.current) 1.0 else 0.0)
    }
}
