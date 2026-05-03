package me.miki.shindo.ui.animation.v2.core

import me.miki.shindo.utils.TimerUtils

open class Animation(
    var duration: Int,
    var endPoint: Double,
    private val equation: (elapsed: Double, duration: Int) -> Double,
    direction: Direction = Direction.FORWARDS
) {
    var direction: Direction = direction
        private set

    private val timer = TimerUtils()

    init {
        timer.reset()
        if (direction == Direction.BACKWARDS) {
            setDirection(direction)
        }
    }

    fun getValue(): Double {
        if (!GlobalAnimationSettings.enabled) {
            return if (direction == Direction.FORWARDS) endPoint else 0.0
        }
        val durationMs = scaledDuration().coerceAtLeast(1L)
        val elapsed = timer.elapsedTime.coerceAtMost(durationMs)
        return if (direction == Direction.FORWARDS) {
            if (timer.delay(durationMs)) endPoint
            else equation(elapsed.toDouble(), duration) * endPoint
        } else {
            if (timer.delay(durationMs)) 0.0
            else (1.0 - equation(elapsed.toDouble(), duration)) * endPoint
        }
    }

    fun getValueFloat(): Float = getValue().toFloat()

    fun getValueInt(): Int = getValue().toInt()

    fun isDone(): Boolean = timer.delay(scaledDuration())

    fun isDone(dir: Direction): Boolean = isDone() && this.direction == dir

    fun reset() { timer.reset() }

    fun changeDirection() { setDirection(direction.opposite()) }

    fun setDirection(dir: Direction) {
        if (direction == dir) return
        direction = dir
        val durationMs = scaledDuration().coerceAtLeast(1L)
        timer.lastMs = System.currentTimeMillis() -
            (durationMs - timer.elapsedTime.coerceAtMost(durationMs))
    }

    fun setProgress(progress: Double) {
        if (progress !in 0.0..1.0) return
        val durationMs = scaledDuration().coerceAtLeast(1L)
        val elapsed = (progress * durationMs).toLong()
        timer.lastMs = System.currentTimeMillis() - elapsed
    }

    private fun scaledDuration(): Long = GlobalAnimationSettings.scaleDuration(duration.toLong())
}
