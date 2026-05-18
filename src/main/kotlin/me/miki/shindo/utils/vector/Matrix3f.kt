package me.miki.shindo.utils.vector

import java.nio.FloatBuffer
import kotlin.math.abs

class Matrix3f {
    var m00 = 0f
    var m01 = 0f
    var m02 = 0f
    var m10 = 0f
    var m11 = 0f
    var m12 = 0f
    var m20 = 0f
    var m21 = 0f
    var m22 = 0f

    constructor()

    constructor(quaternion: Quaternion) {
        val f = quaternion.i()
        val g = quaternion.j()
        val h = quaternion.k()
        val i = quaternion.r()
        val j = 2.0f * f * f
        val k = 2.0f * g * g
        val l = 2.0f * h * h
        this.m00 = 1.0f - k - l
        this.m11 = 1.0f - l - j
        this.m22 = 1.0f - j - k
        val m = f * g
        val n = g * h
        val o = h * f
        val p = f * i
        val q = g * i
        val r = h * i
        this.m10 = 2.0f * (m + r)
        this.m01 = 2.0f * (m - r)
        this.m20 = 2.0f * (o - q)
        this.m02 = 2.0f * (o + q)
        this.m21 = 2.0f * (n + p)
        this.m12 = 2.0f * (n - p)
    }

    constructor(matrix4f: Matrix4f) {
        this.m00 = matrix4f.m00
        this.m01 = matrix4f.m01
        this.m02 = matrix4f.m02
        this.m10 = matrix4f.m10
        this.m11 = matrix4f.m11
        this.m12 = matrix4f.m12
        this.m20 = matrix4f.m20
        this.m21 = matrix4f.m21
        this.m22 = matrix4f.m22
    }

    constructor(matrix3f: Matrix3f) {
        this.m00 = matrix3f.m00
        this.m01 = matrix3f.m01
        this.m02 = matrix3f.m02
        this.m10 = matrix3f.m10
        this.m11 = matrix3f.m11
        this.m12 = matrix3f.m12
        this.m20 = matrix3f.m20
        this.m21 = matrix3f.m21
        this.m22 = matrix3f.m22
    }

    fun transpose() {
        var f = this.m01
        this.m01 = this.m10
        this.m10 = f
        f = this.m02
        this.m02 = this.m20
        this.m20 = f
        f = this.m12
        this.m12 = this.m21
        this.m21 = f
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this.javaClass != other.javaClass) return false
        val matrix3f = other as Matrix3f
        return (
            java.lang.Float.compare(matrix3f.m00, this.m00) == 0 &&
                java.lang.Float.compare(matrix3f.m01, this.m01) == 0 &&
                java.lang.Float.compare(matrix3f.m02, this.m02) == 0 &&
                java.lang.Float.compare(matrix3f.m10, this.m10) == 0 &&
                java.lang.Float.compare(matrix3f.m11, this.m11) == 0 &&
                java.lang.Float.compare(matrix3f.m12, this.m12) == 0 &&
                java.lang.Float.compare(matrix3f.m20, this.m20) == 0 &&
                java.lang.Float.compare(matrix3f.m21, this.m21) == 0 &&
                java.lang.Float.compare(matrix3f.m22, this.m22) == 0
        )
    }

    override fun hashCode(): Int {
        var i = if (this.m00 != 0.0f) java.lang.Float.floatToIntBits(this.m00) else 0
        i = 31 * i + if (this.m01 != 0.0f) java.lang.Float.floatToIntBits(this.m01) else 0
        i = 31 * i + if (this.m02 != 0.0f) java.lang.Float.floatToIntBits(this.m02) else 0
        i = 31 * i + if (this.m10 != 0.0f) java.lang.Float.floatToIntBits(this.m10) else 0
        i = 31 * i + if (this.m11 != 0.0f) java.lang.Float.floatToIntBits(this.m11) else 0
        i = 31 * i + if (this.m12 != 0.0f) java.lang.Float.floatToIntBits(this.m12) else 0
        i = 31 * i + if (this.m20 != 0.0f) java.lang.Float.floatToIntBits(this.m20) else 0
        i = 31 * i + if (this.m21 != 0.0f) java.lang.Float.floatToIntBits(this.m21) else 0
        i = 31 * i + if (this.m22 != 0.0f) java.lang.Float.floatToIntBits(this.m22) else 0
        return i
    }

    fun load(floatBuffer: FloatBuffer) {
        this.m00 = floatBuffer.get(bufferIndex(0, 0))
        this.m01 = floatBuffer.get(bufferIndex(0, 1))
        this.m02 = floatBuffer.get(bufferIndex(0, 2))
        this.m10 = floatBuffer.get(bufferIndex(1, 0))
        this.m11 = floatBuffer.get(bufferIndex(1, 1))
        this.m12 = floatBuffer.get(bufferIndex(1, 2))
        this.m20 = floatBuffer.get(bufferIndex(2, 0))
        this.m21 = floatBuffer.get(bufferIndex(2, 1))
        this.m22 = floatBuffer.get(bufferIndex(2, 2))
    }

    fun loadTransposed(floatBuffer: FloatBuffer) {
        this.m00 = floatBuffer.get(bufferIndex(0, 0))
        this.m01 = floatBuffer.get(bufferIndex(1, 0))
        this.m02 = floatBuffer.get(bufferIndex(2, 0))
        this.m10 = floatBuffer.get(bufferIndex(0, 1))
        this.m11 = floatBuffer.get(bufferIndex(1, 1))
        this.m12 = floatBuffer.get(bufferIndex(2, 1))
        this.m20 = floatBuffer.get(bufferIndex(0, 2))
        this.m21 = floatBuffer.get(bufferIndex(1, 2))
        this.m22 = floatBuffer.get(bufferIndex(2, 2))
    }

    fun load(
        floatBuffer: FloatBuffer,
        bl: Boolean,
    ) {
        if (bl) {
            loadTransposed(floatBuffer)
        } else {
            load(floatBuffer)
        }
    }

    fun load(matrix3f: Matrix3f) {
        this.m00 = matrix3f.m00
        this.m01 = matrix3f.m01
        this.m02 = matrix3f.m02
        this.m10 = matrix3f.m10
        this.m11 = matrix3f.m11
        this.m12 = matrix3f.m12
        this.m20 = matrix3f.m20
        this.m21 = matrix3f.m21
        this.m22 = matrix3f.m22
    }

    override fun toString(): String =
        (
            "Matrix3f:\n" + this.m00 + " " + this.m01 + " " + this.m02 + "\n" + this.m10 + " " + this.m11 + " " +
                this.m12 +
                "\n" +
                this.m20 +
                " " +
                this.m21 +
                " " +
                this.m22 +
                "\n"
        )

    fun store(floatBuffer: FloatBuffer) {
        floatBuffer.put(bufferIndex(0, 0), this.m00)
        floatBuffer.put(bufferIndex(0, 1), this.m01)
        floatBuffer.put(bufferIndex(0, 2), this.m02)
        floatBuffer.put(bufferIndex(1, 0), this.m10)
        floatBuffer.put(bufferIndex(1, 1), this.m11)
        floatBuffer.put(bufferIndex(1, 2), this.m12)
        floatBuffer.put(bufferIndex(2, 0), this.m20)
        floatBuffer.put(bufferIndex(2, 1), this.m21)
        floatBuffer.put(bufferIndex(2, 2), this.m22)
    }

    fun storeTransposed(floatBuffer: FloatBuffer) {
        floatBuffer.put(bufferIndex(0, 0), this.m00)
        floatBuffer.put(bufferIndex(1, 0), this.m01)
        floatBuffer.put(bufferIndex(2, 0), this.m02)
        floatBuffer.put(bufferIndex(0, 1), this.m10)
        floatBuffer.put(bufferIndex(1, 1), this.m11)
        floatBuffer.put(bufferIndex(2, 1), this.m12)
        floatBuffer.put(bufferIndex(0, 2), this.m20)
        floatBuffer.put(bufferIndex(1, 2), this.m21)
        floatBuffer.put(bufferIndex(2, 2), this.m22)
    }

    fun store(
        floatBuffer: FloatBuffer,
        bl: Boolean,
    ) {
        if (bl) {
            storeTransposed(floatBuffer)
        } else {
            store(floatBuffer)
        }
    }

    fun setIdentity() {
        this.m00 = 1.0f
        this.m01 = 0.0f
        this.m02 = 0.0f
        this.m10 = 0.0f
        this.m11 = 1.0f
        this.m12 = 0.0f
        this.m20 = 0.0f
        this.m21 = 0.0f
        this.m22 = 1.0f
    }

    fun adjugateAndDet(): Float {
        val f = this.m11 * this.m22 - this.m12 * this.m21
        val g = -(this.m10 * this.m22 - this.m12 * this.m20)
        val h = this.m10 * this.m21 - this.m11 * this.m20
        val i = -(this.m01 * this.m22 - this.m02 * this.m21)
        val j = this.m00 * this.m22 - this.m02 * this.m20
        val k = -(this.m00 * this.m21 - this.m01 * this.m20)
        val l = this.m01 * this.m12 - this.m02 * this.m11
        val m = -(this.m00 * this.m12 - this.m02 * this.m10)
        val n = this.m00 * this.m11 - this.m01 * this.m10
        val o = this.m00 * f + this.m01 * g + this.m02 * h
        this.m00 = f
        this.m10 = g
        this.m20 = h
        this.m01 = i
        this.m11 = j
        this.m21 = k
        this.m02 = l
        this.m12 = m
        this.m22 = n
        return o
    }

    fun determinant(): Float {
        val f = this.m11 * this.m22 - this.m12 * this.m21
        val g = -(this.m10 * this.m22 - this.m12 * this.m20)
        val h = this.m10 * this.m21 - this.m11 * this.m20
        return this.m00 * f + this.m01 * g + this.m02 * h
    }

    fun invert(): Boolean {
        val f = adjugateAndDet()
        return if (abs(f) > 1.0e-6f) {
            mul(f)
            true
        } else {
            false
        }
    }

    operator fun set(
        i: Int,
        j: Int,
        f: Float,
    ) {
        if (i == 0) {
            if (j == 0) {
                this.m00 = f
            } else if (j == 1) {
                this.m01 = f
            } else {
                this.m02 = f
            }
        } else if (i == 1) {
            if (j == 0) {
                this.m10 = f
            } else if (j == 1) {
                this.m11 = f
            } else {
                this.m12 = f
            }
        } else if (j == 0) {
            this.m20 = f
        } else if (j == 1) {
            this.m21 = f
        } else {
            this.m22 = f
        }
    }

    fun mul(matrix3f: Matrix3f) {
        val f = this.m00 * matrix3f.m00 + this.m01 * matrix3f.m10 + this.m02 * matrix3f.m20
        val g = this.m00 * matrix3f.m01 + this.m01 * matrix3f.m11 + this.m02 * matrix3f.m21
        val h = this.m00 * matrix3f.m02 + this.m01 * matrix3f.m12 + this.m02 * matrix3f.m22
        val i = this.m10 * matrix3f.m00 + this.m11 * matrix3f.m10 + this.m12 * matrix3f.m20
        val j = this.m10 * matrix3f.m01 + this.m11 * matrix3f.m11 + this.m12 * matrix3f.m21
        val k = this.m10 * matrix3f.m02 + this.m11 * matrix3f.m12 + this.m12 * matrix3f.m22
        val l = this.m20 * matrix3f.m00 + this.m21 * matrix3f.m10 + this.m22 * matrix3f.m20
        val m = this.m20 * matrix3f.m01 + this.m21 * matrix3f.m11 + this.m22 * matrix3f.m21
        val n = this.m20 * matrix3f.m02 + this.m21 * matrix3f.m12 + this.m22 * matrix3f.m22
        this.m00 = f
        this.m01 = g
        this.m02 = h
        this.m10 = i
        this.m11 = j
        this.m12 = k
        this.m20 = l
        this.m21 = m
        this.m22 = n
    }

    fun mul(quaternion: Quaternion) {
        mul(Matrix3f(quaternion))
    }

    fun mul(f: Float) {
        this.m00 *= f
        this.m01 *= f
        this.m02 *= f
        this.m10 *= f
        this.m11 *= f
        this.m12 *= f
        this.m20 *= f
        this.m21 *= f
        this.m22 *= f
    }

    fun add(matrix3f: Matrix3f) {
        this.m00 += matrix3f.m00
        this.m01 += matrix3f.m01
        this.m02 += matrix3f.m02
        this.m10 += matrix3f.m10
        this.m11 += matrix3f.m11
        this.m12 += matrix3f.m12
        this.m20 += matrix3f.m20
        this.m21 += matrix3f.m21
        this.m22 += matrix3f.m22
    }

    fun sub(matrix3f: Matrix3f) {
        this.m00 -= matrix3f.m00
        this.m01 -= matrix3f.m01
        this.m02 -= matrix3f.m02
        this.m10 -= matrix3f.m10
        this.m11 -= matrix3f.m11
        this.m12 -= matrix3f.m12
        this.m20 -= matrix3f.m20
        this.m21 -= matrix3f.m21
        this.m22 -= matrix3f.m22
    }

    fun trace(): Float = this.m00 + this.m11 + this.m22

    fun copy(): Matrix3f = Matrix3f(this)

    companion object {
        @JvmStatic
        fun createScaleMatrix(
            f: Float,
            g: Float,
            h: Float,
        ): Matrix3f {
            val matrix3f = Matrix3f()
            matrix3f.m00 = f
            matrix3f.m11 = g
            matrix3f.m22 = h
            return matrix3f
        }

        private fun bufferIndex(
            i: Int,
            j: Int,
        ): Int = j * 3 + i
    }
}
