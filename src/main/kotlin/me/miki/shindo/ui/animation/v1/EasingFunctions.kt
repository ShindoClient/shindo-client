package me.miki.shindo.ui.animation.v1

import me.miki.shindo.ui.animation.v1.EasingFunctions.linear
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object EasingFunctions {

    private val customEasings = mutableMapOf<String, (Double, Int) -> Double>()

    /**
     * Registers a named easing curve so callers can extend easing behaviour without touching this file.
     */
    fun registerCustomEasing(name: String, equation: (Double, Int) -> Double) {
        customEasings[name] = equation
    }

    /**
     * Removes a previously registered custom easing curve by [name].
     */
    fun unregisterCustomEasing(name: String) {
        customEasings.remove(name)
    }

    /**
     * Executes a registered custom easing or falls back to [linear] when the name is unknown.
     */
    fun customEasing(
        name: String,
        elapsed: Double,
        duration: Int,
        fallback: ((Double, Int) -> Double)? = null
    ): Double {
        val easing = customEasings[name]
        return if (easing != null) {
            easing(elapsed, duration)
        } else {
            fallback?.invoke(elapsed, duration) ?: linear(elapsed, duration)
        }
    }

    fun linear(elapsed: Double, duration: Int): Double = elapsed / duration

    fun smoothStep(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration
        return -2 * x1.pow(3) + 3 * x1.pow(2)
    }

    fun decelerate(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration
        return 1 - (x1 - 1).pow(2)
    }

    fun inOutCirc(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration - 1
        return sqrt(1 - x1 * x1)
    }

    fun backIn(elapsed: Double, duration: Int, easeAmount: Float): Double {
        val x1 = elapsed / duration
        val shrink = easeAmount + 1
        return maxOf(0.0, 1 + shrink * (x1 - 1).pow(3) + easeAmount * (x1 - 1).pow(2))
    }

    fun inQuad(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration
        return x1 * x1
    }

    fun outQuad(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration
        return -x1 * (x1 - 2)
    }

    fun inOutQuad(elapsed: Double, duration: Int): Double {
        var x1 = elapsed / (duration / 2.0)
        if (x1 < 1) return 0.5 * x1 * x1
        x1 -= 1
        return -0.5 * (x1 * (x1 - 2) - 1)
    }

    fun inCubic(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration
        return x1 * x1 * x1
    }

    fun outCubic(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration - 1
        return x1 * x1 * x1 + 1
    }

    fun inOutCubic(elapsed: Double, duration: Int): Double {
        var x1 = elapsed / (duration / 2.0)
        if (x1 < 1) return 0.5 * x1 * x1 * x1
        x1 -= 2
        return 0.5 * (x1 * x1 * x1 + 2)
    }

    fun inCirc(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration
        return -1 * (sqrt(1 - x1 * x1) - 1)
    }

    fun outCirc(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration - 1
        return sqrt(1 - x1 * x1)
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
        val x1 = elapsed / (duration / 2.0)
        return if (x1 < 1) 0.5 * 2.0.pow(10 * (x1 - 1))
        else 0.5 * (-2.0.pow(-10 * (x1 - 1)) + 2)
    }

    fun inQuart(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration
        return x1 * x1 * x1 * x1
    }

    fun outQuart(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration - 1
        return -(x1 * x1 * x1 * x1 - 1)
    }

    fun inOutQuart(elapsed: Double, duration: Int): Double {
        var x1 = elapsed / (duration / 2.0)
        if (x1 < 1) return 0.5 * x1 * x1 * x1 * x1
        x1 -= 2
        return -0.5 * (x1 * x1 * x1 * x1 - 2)
    }

    fun inQuint(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration
        return x1 * x1 * x1 * x1 * x1
    }

    fun outQuint(elapsed: Double, duration: Int): Double {
        val x1 = elapsed / duration - 1
        return x1 * x1 * x1 * x1 * x1 + 1
    }

    fun inOutQuint(elapsed: Double, duration: Int): Double {
        var x1 = elapsed / (duration / 2.0)
        if (x1 < 1) return 0.5 * x1 * x1 * x1 * x1 * x1
        x1 -= 2
        return 0.5 * (x1 * x1 * x1 * x1 * x1 + 2)
    }

    fun elastic(
        elapsed: Double,
        duration: Int,
        elasticity: Float,
        smooth: Float,
        reallyElastic: Boolean
    ): Double {
        val x1 = (elapsed / duration).pow(smooth.toDouble())
        val el = elasticity * 0.1f
        return 2.0.pow(-10 * if (reallyElastic) sqrt(x1) else x1) *
                sin((x1 - el / 4) * (2 * Math.PI / el)) + 1
    }

    /**
     * Back easing (overshoot) for the second half of the curve.
     */
    fun outBack(elapsed: Double, duration: Int, easeAmount: Double = 1.70158): Double {
        val t = elapsed / duration
        val c1 = easeAmount
        val c3 = c1 + 1
        val t1 = t - 1
        return 1 + c3 * t1.pow(3) + c1 * t1.pow(2)
    }

    /**
     * Symmetric in/out back easing with configurable overshoot.
     */
    fun inOutBack(elapsed: Double, duration: Int, easeAmount: Double = 1.70158): Double {
        val t = elapsed / duration
        val c1 = easeAmount
        val c2 = c1 * 1.525
        return if (t < 0.5) {
            val twoT = 2 * t
            (twoT.pow(2) * ((c2 + 1) * twoT - c2)) / 2
        } else {
            val twoT = 2 * t - 2
            (twoT.pow(2) * ((c2 + 1) * twoT + c2) + 2) / 2
        }
    }
}
