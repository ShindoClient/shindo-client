package me.miki.shindo.utils.vector

import me.miki.shindo.utils.MathUtils
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

class Quaternion(
    private var i: Float,
    private var j: Float,
    private var k: Float,
    private var r: Float
) {

    constructor(vector3f: Vector3f, f: Float, bl: Boolean) : this(0f, 0f, 0f, 1f) {
        var angle = f
        if (bl) {
            angle *= 0.017453292f
        }
        val g = sin(angle / 2.0f)
        this.i = vector3f.x() * g
        this.j = vector3f.y() * g
        this.k = vector3f.z() * g
        this.r = cos(angle / 2.0f)
    }

    constructor(f: Float, g: Float, h: Float, bl: Boolean) : this(0f, 0f, 0f, 1f) {
        var xAngle = f
        var yAngle = g
        var zAngle = h
        if (bl) {
            xAngle *= 0.017453292f
            yAngle *= 0.017453292f
            zAngle *= 0.017453292f
        }
        val i = sin(0.5f * xAngle)
        val j = cos(0.5f * xAngle)
        val k = sin(0.5f * yAngle)
        val l = cos(0.5f * yAngle)
        val m = sin(0.5f * zAngle)
        val n = cos(0.5f * zAngle)
        this.i = i * l * n + j * k * m
        this.j = j * k * n - i * l * m
        this.k = i * k * n + j * l * m
        this.r = j * l * n - i * k * m
    }

    constructor(quaternion: Quaternion) : this(quaternion.i, quaternion.j, quaternion.k, quaternion.r)

    fun i(): Float = this.i
    fun j(): Float = this.j
    fun k(): Float = this.k
    fun r(): Float = this.r

    fun mul(quaternion: Quaternion) {
        val f = i()
        val g = j()
        val h = k()
        val i = r()
        val j = quaternion.i()
        val k = quaternion.j()
        val l = quaternion.k()
        val m = quaternion.r()
        this.i = i * j + f * m + g * l - h * k
        this.j = i * k - f * l + g * m + h * j
        this.k = i * l + f * k - g * j + h * m
        this.r = i * m - f * j - g * k - h * l
    }

    fun mul(f: Float) {
        this.i *= f
        this.j *= f
        this.k *= f
        this.r *= f
    }

    fun conj() {
        this.i = -this.i
        this.j = -this.j
        this.k = -this.k
    }

    fun set(f: Float, g: Float, h: Float, i: Float) {
        this.i = f
        this.j = g
        this.k = h
        this.r = i
    }

    fun normalize() {
        val f = i() * i() + j() * j() + k() * k() + r() * r()

        if (f > 1.0e-6f) {
            val g = MathUtils.fastInvSqrt(f.toFloat())
            this.i *= g
            this.j *= g
            this.k *= g
            this.r *= g
        } else {
            this.i = 0.0f
            this.j = 0.0f
            this.k = 0.0f
            this.r = 0.0f
        }
    }

    fun slerp(quaternion: Quaternion, f: Float) {
        throw UnsupportedOperationException()
    }

    fun copy(): Quaternion = Quaternion(this)

    fun toXYZ(): Vector3f {
        val f = r() * r()
        val g = i() * i()
        val h = j() * j()
        val i = k() * k()
        val j = f + g + h + i
        val k = 2.0f * r() * i() - 2.0f * j() * k()
        val l = asin(k / j)
        return if (abs(k) > 0.999f * j) {
            Vector3f(2.0f * atan2(i().toDouble(), r().toDouble()).toFloat(), l, 0.0f)
        } else {
            Vector3f(
                atan2((2.0f * j() * k() + 2.0f * i() * r()).toDouble(), (f - g - h + i).toDouble()).toFloat(),
                l,
                atan2((2.0f * i() * j() + 2.0f * r() * k()).toDouble(), (f + g - h - i).toDouble()).toFloat()
            )
        }
    }

    fun toXYZDegrees(): Vector3f {
        val vector3f = toXYZ()
        return Vector3f(
            Math.toDegrees(vector3f.x().toDouble()).toFloat(),
            Math.toDegrees(vector3f.y().toDouble()).toFloat(),
            Math.toDegrees(vector3f.z().toDouble()).toFloat()
        )
    }

    fun toYXZ(): Vector3f {
        val f = r() * r()
        val g = i() * i()
        val h = j() * j()
        val i = k() * k()
        val j = f + g + h + i
        val k = 2.0f * r() * i() - 2.0f * j() * k()
        val l = asin(k / j)
        return if (abs(k) > 0.999f * j) {
            Vector3f(l, 2.0f * atan2(j().toDouble(), r().toDouble()).toFloat(), 0.0f)
        } else {
            Vector3f(
                l,
                atan2((2.0f * i() * k() + 2.0f * j() * r()).toDouble(), (f - g - h + i).toDouble()).toFloat(),
                atan2((2.0f * i() * j() + 2.0f * r() * k()).toDouble(), (f - g + h - i).toDouble()).toFloat()
            )
        }
    }

    fun toYXZDegrees(): Vector3f {
        val vector3f = toYXZ()
        return Vector3f(
            Math.toDegrees(vector3f.x().toDouble()).toFloat(),
            Math.toDegrees(vector3f.y().toDouble()).toFloat(),
            Math.toDegrees(vector3f.z().toDouble()).toFloat()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other.javaClass != javaClass) return false
        val quaternion = other as Quaternion
        if (java.lang.Float.compare(quaternion.i, this.i) != 0) return false
        if (java.lang.Float.compare(quaternion.j, this.j) != 0) return false
        if (java.lang.Float.compare(quaternion.k, this.k) != 0) return false
        return java.lang.Float.compare(quaternion.r, this.r) == 0
    }

    override fun hashCode(): Int {
        var i = java.lang.Float.floatToIntBits(this.i)
        i = 31 * i + java.lang.Float.floatToIntBits(this.j)
        i = 31 * i + java.lang.Float.floatToIntBits(this.k)
        i = 31 * i + java.lang.Float.floatToIntBits(this.r)
        return i
    }

    override fun toString(): String {
        return "Quaternion[${r()} + ${i()}i + ${j()}j + ${k()}k]"
    }

    companion object {
        @JvmField
        val ONE = Quaternion(0.0f, 0.0f, 0.0f, 1.0f)

        @JvmStatic
        fun fromYXZ(f: Float, g: Float, h: Float): Quaternion {
            val quaternion = ONE.copy()
            quaternion.mul(Quaternion(0.0f, sin(f / 2.0f), 0.0f, cos(f / 2.0f)))
            quaternion.mul(Quaternion(sin(g / 2.0f), 0.0f, 0.0f, cos(g / 2.0f)))
            quaternion.mul(Quaternion(0.0f, 0.0f, sin(h / 2.0f), cos(h / 2.0f)))
            return quaternion
        }

        @JvmStatic
        fun fromXYZDegrees(vector3f: Vector3f): Quaternion {
            return fromXYZ(
                Math.toRadians(vector3f.x().toDouble()).toFloat(),
                Math.toRadians(vector3f.y().toDouble()).toFloat(),
                Math.toRadians(vector3f.z().toDouble()).toFloat()
            )
        }

        @JvmStatic
        fun fromXYZ(vector3f: Vector3f): Quaternion {
            return fromXYZ(vector3f.x(), vector3f.y(), vector3f.z())
        }

        @JvmStatic
        fun fromXYZ(f: Float, g: Float, h: Float): Quaternion {
            val quaternion = ONE.copy()
            quaternion.mul(Quaternion(sin(f / 2.0f), 0.0f, 0.0f, cos(f / 2.0f)))
            quaternion.mul(Quaternion(0.0f, sin(g / 2.0f), 0.0f, cos(g / 2.0f)))
            quaternion.mul(Quaternion(0.0f, 0.0f, sin(h / 2.0f), cos(h / 2.0f)))
            return quaternion
        }
    }
}
