package me.miki.shindo.utils

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.min

object MathUtils {
    @JvmStatic
    fun isOdd(number: Int): Boolean = number % 2 != 0

    @JvmStatic
    fun clamp(value: Float): Float =
        when {
            value < 0.0f -> 0.0f
            value > 1.0f -> 1.0f
            else -> value
        }

    @JvmStatic
    fun clamp(
        number: Float,
        min: Float,
        max: Float,
    ): Float = if (number < min) min else min(number, max)

    @JvmStatic
    fun interpolate(
        oldValue: Double,
        newValue: Double,
        interpolationValue: Double,
    ): Double = oldValue + (newValue - oldValue) * interpolationValue

    @JvmStatic
    fun interpolateFloat(
        oldValue: Float,
        newValue: Float,
        interpolationValue: Double,
    ): Float = interpolate(oldValue.toDouble(), newValue.toDouble(), interpolationValue).toFloat()

    @JvmStatic
    fun interpolateInt(
        oldValue: Int,
        newValue: Int,
        interpolationValue: Double,
    ): Int = interpolate(oldValue.toDouble(), newValue.toDouble(), interpolationValue).toInt()

    @JvmStatic
    fun interpolateARGB(
        start: Int,
        end: Int,
        progress: Float,
    ): Int {
        val a = interpolateInt(start shr 24 and 0xFF, end shr 24 and 0xFF, progress.toDouble())
        val r = interpolateInt(start shr 16 and 0xFF, end shr 16 and 0xFF, progress.toDouble())
        val g = interpolateInt(start shr 8 and 0xFF, end shr 8 and 0xFF, progress.toDouble())
        val b = interpolateInt(start and 0xFF, end and 0xFF, progress.toDouble())
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    @JvmStatic
    fun isInRange(
        value: Float,
        min: Float,
        max: Float,
    ): Boolean = value > min && value < max

    @JvmStatic
    fun sin(value: Double): Float = kotlin.math.sin(value).toFloat()

    @JvmStatic
    fun cos(value: Double): Float = kotlin.math.cos(value).toFloat()

    @JvmStatic
    fun lerp(
        f: Float,
        g: Float,
        h: Float,
    ): Float = g + (h - g) * f

    @JvmStatic
    fun lerp(
        d: Double,
        e: Double,
        f: Double,
    ): Double = e + d * (f - e)

    @JvmStatic
    fun fastInvSqrt(f: Float): Float {
        var value = f
        val g = 0.5f * value
        var i = java.lang.Float.floatToIntBits(value)
        i = 1597463007 - (i shr 1)
        value = java.lang.Float.intBitsToFloat(i)
        value *= 1.5f - g * value * value
        return value
    }

    @JvmStatic
    fun fastInvCubeRoot(f: Double): Double {
        var i = java.lang.Float.floatToIntBits(f.toFloat())
        i = 1419967116 - i / 3
        var g = java.lang.Float.intBitsToFloat(i)
        g = 0.6666667f * g + 1.0f / 3.0f * g * g * f.toFloat()
        g = 0.6666667f * g + 1.0f / 3.0f * g * g * f.toFloat()
        return g.toDouble()
    }

    @JvmStatic
    fun fastInvCubeRoot(f: Float): Float {
        var i = java.lang.Float.floatToIntBits(f)
        i = 1419967116 - i / 3
        var g = java.lang.Float.intBitsToFloat(i)
        g = 0.6666667f * g + 1.0f / 3.0f * g * g * f
        g = 0.6666667f * g + 1.0f / 3.0f * g * g * f
        return g
    }

    @JvmStatic
    fun roundToPlace(
        value: Double,
        places: Int,
    ): Double {
        require(places >= 0) { "places must be non-negative" }
        var bd = BigDecimal(value)
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    @JvmStatic
    fun roundToPlace(
        value: Float,
        places: Int,
    ): Float {
        require(places >= 0) { "places must be non-negative" }
        var bd = BigDecimal(value.toDouble())
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toFloat()
    }

    @JvmStatic
    fun abs(value: Double): Double = if (value >= 0.0f) value else -value

    @JvmStatic
    fun abs(value: Float): Float = if (value >= 0.0f) value else -value
}
