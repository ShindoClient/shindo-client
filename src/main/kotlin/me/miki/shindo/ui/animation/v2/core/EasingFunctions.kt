package me.miki.shindo.ui.animation.v2.core

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object EasingFunctions {

    private val customEasings = mutableMapOf<String, (Double, Int) -> Double>()

    fun registerCustomEasing(name: String, equation: (Double, Int) -> Double) {
        customEasings[name] = equation
    }

    fun unregisterCustomEasing(name: String) {
        customEasings.remove(name)
    }

    fun customEasing(
        name: String,
        elapsed: Double,
        duration: Int,
        fallback: ((Double, Int) -> Double)? = null
    ): Double {
        val easing = customEasings[name]
        return if (easing != null) easing(elapsed, duration)
               else fallback?.invoke(elapsed, duration) ?: linear(elapsed, duration)
    }

    fun linear(elapsed: Double, duration: Int): Double = elapsed / duration

    fun smoothStep(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration
        return -2 * x.pow(3) + 3 * x.pow(2)
    }

    fun decelerate(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration
        return 1 - (x - 1).pow(2)
    }

    fun inOutCirc(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration - 1
        return sqrt(1 - x * x)
    }

    fun backIn(elapsed: Double, duration: Int, easeAmount: Float): Double {
        val x = elapsed / duration
        val shrink = easeAmount + 1
        return maxOf(0.0, 1 + shrink * (x - 1).pow(3) + easeAmount * (x - 1).pow(2))
    }

    fun inQuad(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration
        return x * x
    }

    fun outQuad(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration
        return -x * (x - 2)
    }

    fun inOutQuad(elapsed: Double, duration: Int): Double {
        var x = elapsed / (duration / 2.0)
        if (x < 1) return 0.5 * x * x
        x -= 1
        return -0.5 * (x * (x - 2) - 1)
    }

    fun inCubic(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration
        return x * x * x
    }

    fun outCubic(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration - 1
        return x * x * x + 1
    }

    fun inOutCubic(elapsed: Double, duration: Int): Double {
        var x = elapsed / (duration / 2.0)
        if (x < 1) return 0.5 * x * x * x
        x -= 2
        return 0.5 * (x * x * x + 2)
    }

    fun inCirc(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration
        return -(sqrt(1 - x * x) - 1)
    }

    fun outCirc(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration - 1
        return sqrt(1 - x * x)
    }

    fun inSine(elapsed: Double, duration: Int): Double =
        -cos(Math.PI * elapsed / duration / 2) + 1.0

    fun outSine(elapsed: Double, duration: Int): Double =
        sin(Math.PI * elapsed / duration / 2)

    fun inOutSine(elapsed: Double, duration: Int): Double =
        -0.5 * (cos(Math.PI * elapsed / duration) - 1)

    fun inExpo(elapsed: Double, duration: Int): Double =
        if (elapsed == 0.0) 0.0 else 2.0.pow(10 * (elapsed / duration - 1))

    fun outExpo(elapsed: Double, duration: Int): Double =
        if (elapsed >= duration) 1.0 else -2.0.pow(-10 * elapsed / duration) + 1

    fun inOutExpo(elapsed: Double, duration: Int): Double {
        if (elapsed == 0.0) return 0.0
        if (elapsed >= duration) return 1.0
        val x = elapsed / (duration / 2.0)
        return if (x < 1) 0.5 * 2.0.pow(10 * (x - 1))
               else 0.5 * (-2.0.pow(-10 * (x - 1)) + 2)
    }

    fun inQuart(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration
        return x * x * x * x
    }

    fun outQuart(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration - 1
        return -(x * x * x * x - 1)
    }

    fun inOutQuart(elapsed: Double, duration: Int): Double {
        var x = elapsed / (duration / 2.0)
        if (x < 1) return 0.5 * x * x * x * x
        x -= 2
        return -0.5 * (x * x * x * x - 2)
    }

    fun inQuint(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration
        return x * x * x * x * x
    }

    fun outQuint(elapsed: Double, duration: Int): Double {
        val x = elapsed / duration - 1
        return x * x * x * x * x + 1
    }

    fun inOutQuint(elapsed: Double, duration: Int): Double {
        var x = elapsed / (duration / 2.0)
        if (x < 1) return 0.5 * x * x * x * x * x
        x -= 2
        return 0.5 * (x * x * x * x * x + 2)
    }

    fun elastic(
        elapsed: Double,
        duration: Int,
        elasticity: Float,
        smooth: Float,
        reallyElastic: Boolean
    ): Double {
        val x = (elapsed / duration).pow(smooth.toDouble())
        val el = elasticity * 0.1f
        return 2.0.pow(-10 * if (reallyElastic) sqrt(x) else x) *
               sin((x - el / 4) * (2 * Math.PI / el)) + 1
    }

    fun outBack(elapsed: Double, duration: Int, easeAmount: Double = 1.70158): Double {
        val t = elapsed / duration
        val c3 = easeAmount + 1
        val t1 = t - 1
        return 1 + c3 * t1.pow(3) + easeAmount * t1.pow(2)
    }

    fun inOutBack(elapsed: Double, duration: Int, easeAmount: Double = 1.70158): Double {
        val t = elapsed / duration
        val c2 = easeAmount * 1.525
        return if (t < 0.5) {
            val twoT = 2 * t
            (twoT.pow(2) * ((c2 + 1) * twoT - c2)) / 2
        } else {
            val twoT = 2 * t - 2
            (twoT.pow(2) * ((c2 + 1) * twoT + c2) + 2) / 2
        }
    }
}
