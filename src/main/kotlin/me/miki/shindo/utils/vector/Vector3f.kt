package me.miki.shindo.utils.vector

import me.miki.shindo.utils.MathUtils
import net.minecraft.util.MathHelper

class Vector3f(
    @JvmField var x: Float = 0.0f,
    @JvmField var y: Float = 0.0f,
    @JvmField var z: Float = 0.0f,
) {
    constructor(vector4f: Vector4f) : this(vector4f.x(), vector4f.y(), vector4f.z())

    fun x(): Float = x

    fun y(): Float = y

    fun z(): Float = z

    fun mul(f: Float) {
        this.x *= f
        this.y *= f
        this.z *= f
    }

    fun mul(
        f: Float,
        g: Float,
        h: Float,
    ) {
        this.x *= f
        this.y *= g
        this.z *= h
    }

    fun clamp(
        min: Vector3f,
        max: Vector3f,
    ) {
        this.x = MathHelper.clamp_float(this.x, min.x(), max.x())
        this.y = MathHelper.clamp_float(this.y, min.x(), max.y())
        this.z = MathHelper.clamp_float(this.z, min.z(), max.z())
    }

    fun clamp(
        f: Float,
        g: Float,
    ) {
        this.x = MathHelper.clamp_float(this.x, f, g)
        this.y = MathHelper.clamp_float(this.y, f, g)
        this.z = MathHelper.clamp_float(this.z, f, g)
    }

    fun set(
        f: Float,
        g: Float,
        h: Float,
    ) {
        this.x = f
        this.y = g
        this.z = h
    }

    fun load(other: Vector3f) {
        this.x = other.x
        this.y = other.y
        this.z = other.z
    }

    fun add(
        f: Float,
        g: Float,
        h: Float,
    ) {
        this.x += f
        this.y += g
        this.z += h
    }

    fun add(other: Vector3f) {
        this.x += other.x
        this.y += other.y
        this.z += other.z
    }

    fun sub(other: Vector3f) {
        this.x -= other.x
        this.y -= other.y
        this.z -= other.z
    }

    fun dot(other: Vector3f): Float = this.x * other.x + this.y * other.y + this.z * other.z

    fun normalize(): Boolean {
        val f = this.x * this.x + this.y * this.y + this.z * this.z
        if (f < 1.0e-5) {
            return false
        }
        val g = MathUtils.fastInvSqrt(f.toFloat())
        this.x *= g
        this.y *= g
        this.z *= g
        return true
    }

    fun cross(other: Vector3f) {
        val f = this.x
        val g = this.y
        val h = this.z
        val i = other.x()
        val j = other.y()
        val k = other.z()
        this.x = g * k - h * j
        this.y = h * i - f * k
        this.z = f * j - g * i
    }

    fun transform(quaternion: Quaternion) {
        val q2 = Quaternion(quaternion)
        q2.mul(Quaternion(x(), y(), z(), 0.0f))
        val q3 = Quaternion(quaternion)
        q3.conj()
        q2.mul(q3)
        set(q2.i(), q2.j(), q2.k())
    }

    fun lerp(
        other: Vector3f,
        f: Float,
    ) {
        val g = 1.0f - f
        this.x = this.x * g + other.x * f
        this.y = this.y * g + other.y * f
        this.z = this.z * g + other.z * f
    }

    fun rotation(f: Float): Quaternion = Quaternion(this, f, false)

    fun rotationDegrees(f: Float): Quaternion = Quaternion(this, f, true)

    fun copy(): Vector3f = Vector3f(this.x, this.y, this.z)

    override fun toString(): String = "[$x, $y, $z]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other.javaClass != javaClass) return false
        val vector3f = other as Vector3f
        if (vector3f.x.compareTo(this.x) != 0) return false
        if (vector3f.y.compareTo(this.y) != 0) return false
        return vector3f.z.compareTo(this.z) == 0
    }

    override fun hashCode(): Int {
        var i = java.lang.Float.floatToIntBits(this.x)
        i = 31 * i + java.lang.Float.floatToIntBits(this.y)
        i = 31 * i + java.lang.Float.floatToIntBits(this.z)
        return i
    }

    companion object {
        @JvmField
        val XN = Vector3f(-1.0f, 0.0f, 0.0f)

        @JvmField
        val XP = Vector3f(1.0f, 0.0f, 0.0f)

        @JvmField
        val YN = Vector3f(0.0f, -1.0f, 0.0f)

        @JvmField
        val YP = Vector3f(0.0f, 1.0f, 0.0f)

        @JvmField
        val ZN = Vector3f(0.0f, 0.0f, -1.0f)

        @JvmField
        val ZP = Vector3f(0.0f, 0.0f, 1.0f)

        @JvmField
        val ZERO = Vector3f(0.0f, 0.0f, 0.0f)
    }
}
