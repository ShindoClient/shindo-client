package me.miki.shindo.ui.animation.v1

import me.miki.shindo.utils.TimerUtils

/**
 * Core timeline that advances from 0.0 to [endPoint] over [duration] milliseconds.
 *
 * Duration is scaled by [GlobalAnimationSettings.animationScale] whenever a value is read;
 * when animations are globally disabled the value jumps immediately to the target.
 */
abstract class Animation {

    var duration: Int
        protected set

    var endPoint: Double
        protected set

    var direction: Direction = Direction.FORWARDS
        private set

    protected val timer = TimerUtils()

    constructor(ms: Int, endPoint: Double) {
        this.duration = ms
        this.endPoint = endPoint
    }

    constructor(ms: Int, endPoint: Double, direction: Direction) {
        this.duration = ms
        this.endPoint = endPoint
        setDirection(direction)
    }

    /** Returns true when finished in the specified [dir]. */
    fun isDone(dir: Direction): Boolean = isDone() && this.direction == dir

    /** Linear progress based on scaled duration (ignores easing). */
    fun getLinearOutput(): Double {
        val durationMs = scaledDuration().coerceAtLeast(1L).toDouble()
        return 1 - (timer.elapsedTime.toDouble() / durationMs) * endPoint
    }

    fun reset() {
        timer.reset()
    }

    /** Returns true when the scaled duration has elapsed. */
    fun isDone(): Boolean = timer.delay(scaledDuration())

    fun changeDirection() {
        setDirection(direction.opposite())
    }

    /**
     * Override to adapt backwards output for non-symmetric easings.
     * Default keeps the forward equation and mirrors it instead of recomputing.
     */
    protected open fun correctOutput(): Boolean = false

    /** Current eased value respecting direction, scaling, and global disable flag. */
    fun getValue(): Double {
        if (!GlobalAnimationSettings.enabled) {
            return if (direction == Direction.FORWARDS) endPoint else 0.0
        }
        val durationMs = scaledDuration().coerceAtLeast(1L)
        val elapsed = timer.elapsedTime.coerceAtMost(durationMs)
        return if (direction == Direction.FORWARDS) {
            if (timer.delay(durationMs)) endPoint else getEquation(elapsed.toDouble(), durationMs) * endPoint
        } else {
            if (timer.delay(durationMs)) 0.0
            else if (correctOutput()) {
                val revTime = durationMs - elapsed
                getEquation(revTime.toDouble(), durationMs) * endPoint
            } else {
                (1 - getEquation(elapsed.toDouble(), durationMs)) * endPoint
            }
        }
    }

    /**
     * Jumps to a specific progress [value] (0..1) while keeping elapsed time coherent for future calls.
     */
    fun setValue(value: Double) {
        if (value in 0.0..1.0) {
            endPoint = value
            val durationMs = scaledDuration().coerceAtLeast(1L)
            val elapsedTime = ((1 - value) * durationMs).toLong()
            timer.lastMs = System.currentTimeMillis() - (durationMs - elapsedTime.coerceAtMost(durationMs))
        }
    }

    /** Changes animation direction while preserving elapsed time across scaled duration. */
    fun setDirection(dir: Direction) {
        if (direction != dir) {
            direction = dir
            val durationMs = scaledDuration().coerceAtLeast(1L)
            timer.lastMs =
                System.currentTimeMillis() - (durationMs - timer.elapsedTime.coerceAtMost(durationMs))
        }
    }

    /** Convenience float accessor to avoid boxing a Double each frame. */
    fun getValueFloat(): Float = getValue().toFloat()

    /** Convenience int accessor for callers that need integer coordinates. */
    fun getValueInt(): Int = getValue().toInt()

    protected abstract fun getEquation(x: Double): Double

    private fun getEquation(x: Double, durationMs: Long): Double =
        getEquation(x * duration / durationMs.toDouble())

    private fun scaledDuration(): Long = GlobalAnimationSettings.scaleDuration(duration.toLong())
}
