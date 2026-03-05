package me.miki.shindo.ui.animation

import me.miki.shindo.utils.TimerUtils

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

    fun isDone(dir: Direction): Boolean = isDone() && this.direction == dir

    fun getLinearOutput(): Double = 1 - (timer.elapsedTime.toDouble() / duration) * endPoint

    fun reset() {
        timer.reset()
    }

    fun isDone(): Boolean = timer.delay(duration.toLong())

    fun changeDirection() {
        setDirection(direction.opposite())
    }

    protected open fun correctOutput(): Boolean = false

    fun getValue(): Double {
        if (!GlobalAnimationSettings.enabled) {
            return if (direction == Direction.FORWARDS) endPoint else 0.0
        }
        return if (direction == Direction.FORWARDS) {
            if (isDone()) endPoint else getEquation(timer.elapsedTime.toDouble()) * endPoint
        } else {
            if (isDone()) 0.0
            else if (correctOutput()) {
                val revTime = duration.coerceIn(0, duration) - timer.elapsedTime
                getEquation(revTime.toDouble()) * endPoint
            } else {
                (1 - getEquation(timer.elapsedTime.toDouble())) * endPoint
            }
        }
    }

    fun setValue(value: Double) {
        if (value in 0.0..1.0) {
            endPoint = value
            val elapsedTime = ((1 - value) * duration).toLong()
            timer.lastMs = System.currentTimeMillis() - (duration - elapsedTime.coerceAtMost(duration.toLong()))
        }
    }

    fun setDirection(dir: Direction) {
        if (direction != dir) {
            direction = dir
            timer.lastMs = System.currentTimeMillis() - (duration - timer.elapsedTime.coerceAtMost(duration.toLong()))
        }
    }

    fun getValueFloat(): Float = getValue().toFloat()

    fun getValueInt(): Int = getValue().toInt()

    protected abstract fun getEquation(x: Double): Double
}
