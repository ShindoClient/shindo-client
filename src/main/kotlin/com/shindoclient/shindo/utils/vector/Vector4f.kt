package com.shindoclient.shindo.utils.vector

import com.shindoclient.shindo.utils.MathUtils

class Vector4f(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var z: Float = 0.0f,
    var w: Float = 0.0f,
) {
    fun x(): Float = x

    fun y(): Float = y

    fun z(): Float = z

    fun w(): Float = w

    fun mul(f: Float) {
        this.x *= f
        this.y *= f
        this.z *= f
        this.w *= f
    }

    fun set(
        f: Float,
        g: Float,
        h: Float,
        i: Float,
    ) {
        this.x = f
        this.y = g
        this.z = h
        this.w = i
    }

    fun add(
        f: Float,
        g: Float,
        h: Float,
        i: Float,
    ) {
        this.x += f
        this.y += g
        this.z += h
        this.w += i
    }

    fun dot(vector4f: Vector4f): Float = this.x * vector4f.x + this.y * vector4f.y + this.z * vector4f.z + this.w * vector4f.w

    fun normalize(): Boolean {
        val f = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w
        if (f < 1.0e-5) {
            return false
        }
        val g = MathUtils.fastInvSqrt(f.toFloat())
        this.x *= g
        this.y *= g
        this.z *= g
        this.w *= g
        return true
    }

    fun transform(matrix4f: Matrix4f) {
        val f = this.x
        val g = this.y
        val h = this.z
        val i = this.w
        this.x = matrix4f.m00 * f + matrix4f.m01 * g + matrix4f.m02 * h + matrix4f.m03 * i
        this.y = matrix4f.m10 * f + matrix4f.m11 * g + matrix4f.m12 * h + matrix4f.m13 * i
        this.z = matrix4f.m20 * f + matrix4f.m21 * g + matrix4f.m22 * h + matrix4f.m23 * i
        this.w = matrix4f.m30 * f + matrix4f.m31 * g + matrix4f.m32 * h + matrix4f.m33 * i
    }

    fun transform(quaternion: Quaternion) {
        val quaternion2 = Quaternion(quaternion)
        quaternion2.mul(Quaternion(x(), y(), z(), 0.0f))
        val quaternion3 = Quaternion(quaternion)
        quaternion3.conj()
        quaternion2.mul(quaternion3)
        set(quaternion2.i(), quaternion2.j(), quaternion2.k(), w())
    }

    fun perspectiveDivide() {
        this.x /= this.w
        this.y /= this.w
        this.z /= this.w
        this.w = 1.0f
    }

    fun lerp(
        vector4f: Vector4f,
        f: Float,
    ) {
        val g = 1.0f - f
        this.x = this.x * g + vector4f.x * f
        this.y = this.y * g + vector4f.y * f
        this.z = this.z * g + vector4f.z * f
        this.w = this.w * g + vector4f.w * f
    }

    override fun toString(): String = "[$x, $y, $z, $w]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other.javaClass != javaClass) return false
        val vector4f = other as Vector4f
        if (java.lang.Float.compare(vector4f.x, this.x) != 0) return false
        if (java.lang.Float.compare(vector4f.y, this.y) != 0) return false
        if (java.lang.Float.compare(vector4f.z, this.z) != 0) return false
        return java.lang.Float.compare(vector4f.w, this.w) == 0
    }

    override fun hashCode(): Int {
        var i = java.lang.Float.floatToIntBits(this.x)
        i = 31 * i + java.lang.Float.floatToIntBits(this.y)
        i = 31 * i + java.lang.Float.floatToIntBits(this.z)
        i = 31 * i + java.lang.Float.floatToIntBits(this.w)
        return i
    }
}
