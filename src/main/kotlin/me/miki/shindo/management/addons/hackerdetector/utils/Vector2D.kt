package me.miki.shindo.management.addons.hackerdetector.utils

import net.minecraft.util.MathHelper
import kotlin.math.acos
import kotlin.math.sqrt

data class Vector2D(val u: Double, val v: Double) {

    fun norm(): Double = sqrt(u * u + v * v)

    fun getAngle(): Double {
        val norm = this.norm()
        if (norm < 1.0000000116860974E-7) return 0.0

        val cos = u / norm
        return when {
            cos > 1 -> 0.0
            cos < -1 -> 180.0
            else -> Math.toDegrees(acos(cos))
        }
    }

    fun getOrientedAngle(): Double {
        val norm = this.norm()
        if (norm < 1.0000000116860974E-7) return 0.0

        val cos = u / norm
        val angle = when {
            cos > 1 -> 0.0
            cos < -1 -> 180.0
            else -> Math.toDegrees(acos(cos))
        }

        return if (v >= 0) angle else -angle
    }

    private fun dotProduct(other: Vector2D): Double = u * other.u + v * other.v

    fun getAngleWithVector(other: Vector2D): Double {
        val den = sqrt((u * u + v * v) * (other.u * other.u + other.v * other.v))
        if (den < 1.0000000116860974E-7) return 0.0

        val cos = dotProduct(other) / den
        return when {
            cos > 1 -> 0.0
            cos < -1 -> 180.0
            else -> Math.toDegrees(acos(cos))
        }
    }

    companion object {
        fun getVectorFromRotation(pitch: Float, yaw: Float): Vector2D {
            val f = MathHelper.cos(-yaw * 0.017453292F - Math.PI.toFloat())
            val f1 = MathHelper.sin(-yaw * 0.017453292F - Math.PI.toFloat())
            val f2 = -MathHelper.cos(-pitch * 0.017453292F)
            return Vector2D((f1 * f2).toDouble(), (f * f2).toDouble())
        }
    }

    override fun toString(): String = "{${String.format("%.4f", u)}, ${String.format("%.4f", v)}}"
}
