package com.shindoclient.shindo.ui.animation.v2

import com.shindoclient.shindo.utils.TimerUtils
import kotlin.math.max
import kotlin.math.min

abstract class Animation {
    @JvmField
    val timer = TimerUtils()

    private var duration: Int
    private var endPoint: Double
    private var direction: Direction

    constructor(ms: Int, endPoint: Double) {
        this.duration = ms
        this.endPoint = endPoint
        this.direction = Direction.FORWARDS
    }

    constructor(ms: Int, endPoint: Double, direction: Direction) {
        this.duration = ms
        this.endPoint = endPoint
        this.direction = direction
    }

    fun isDone(dir: Direction): Boolean = isDone() && this.direction == dir

    fun getLinearOutput(): Double = 1 - ((timer.elapsedTime / duration.toDouble()) * endPoint)

    fun reset() {
        timer.reset()
    }

    fun isDone(): Boolean = timer.delay(duration.toLong())

    fun changeDirection() {
        setDirection(direction.opposite())
    }

    fun setDirection(direction: Direction) {
        if (this.direction != direction) {
            this.direction = direction
            timer.lastMs = System.currentTimeMillis() - (duration - min(duration.toLong(), timer.elapsedTime))
        }
    }

    open fun correctOutput(): Boolean = false

    fun getValue(): Double {
        if (!GlobalAnimationSettings.enabled) {
            return if (direction == Direction.FORWARDS) endPoint else 0.0
        }
        if (direction == Direction.FORWARDS) {
            if (isDone()) return endPoint
            return (getEquation(timer.elapsedTime.toDouble()) * endPoint)
        } else {
            if (isDone()) return 0.0
            if (correctOutput()) {
                val revTime = min(duration.toLong(), max(0, duration - timer.elapsedTime)).toDouble()
                return getEquation(revTime) * endPoint
            } else {
                return (1 - getEquation(timer.elapsedTime.toDouble())) * endPoint
            }
        }
    }

    fun setValue(value: Double) {
        if (value in 0.0..1.0) {
            this.endPoint = value
            val elapsedTime = ((1 - value) * duration).toLong()
            timer.lastMs = System.currentTimeMillis() - (duration - min(duration.toLong(), elapsedTime))
        }
    }

    fun getValueFloat(): Float = getValue().toFloat()

    fun getValueInt(): Int = getValue().toInt()

    protected abstract fun getEquation(x: Double): Double

    fun getEndPoint(): Double = endPoint

    fun setEndPoint(endPoint: Double) {
        this.endPoint = endPoint
    }

    fun getDuration(): Int = duration

    fun getDirection(): Direction = direction
}
