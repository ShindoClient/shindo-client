package me.miki.shindo.utils.vector

import java.nio.FloatBuffer

class Matrix4f {
    var m00 = 0f
    var m01 = 0f
    var m02 = 0f
    var m03 = 0f
    var m10 = 0f
    var m11 = 0f
    var m12 = 0f
    var m13 = 0f
    var m20 = 0f
    var m21 = 0f
    var m22 = 0f
    var m23 = 0f
    var m30 = 0f
    var m31 = 0f
    var m32 = 0f
    var m33 = 0f

    constructor()

    constructor(matrix4f: Matrix4f) {
        this.m00 = matrix4f.m00
        this.m01 = matrix4f.m01
        this.m02 = matrix4f.m02
        this.m03 = matrix4f.m03
        this.m10 = matrix4f.m10
        this.m11 = matrix4f.m11
        this.m12 = matrix4f.m12
        this.m13 = matrix4f.m13
        this.m20 = matrix4f.m20
        this.m21 = matrix4f.m21
        this.m22 = matrix4f.m22
        this.m23 = matrix4f.m23
        this.m30 = matrix4f.m30
        this.m31 = matrix4f.m31
        this.m32 = matrix4f.m32
        this.m33 = matrix4f.m33
    }

    constructor(matrix3f: Matrix3f) {
        this.m00 = matrix3f.m00
        this.m01 = matrix3f.m01
        this.m02 = matrix3f.m02
        this.m03 = 0.0f
        this.m10 = matrix3f.m10
        this.m11 = matrix3f.m11
        this.m12 = matrix3f.m12
        this.m13 = 0.0f
        this.m20 = matrix3f.m20
        this.m21 = matrix3f.m21
        this.m22 = matrix3f.m22
        this.m23 = 0.0f
        this.m30 = 0.0f
        this.m31 = 0.0f
        this.m32 = 0.0f
        this.m33 = 1.0f
    }

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
        this.m03 = 0.0f
        this.m13 = 0.0f
        this.m23 = 0.0f
        this.m30 = 0.0f
        this.m31 = 0.0f
        this.m32 = 0.0f
        this.m33 = 1.0f
    }

    private fun cofac(
        i: Int,
        j: Int,
    ): Float {
        var f = (1 + (i + j) % 2 * 2) * get(i + 1, j + 1) * get(i + 2, j + 2)
        f -= (1 + (i + j + 1) % 2 * 2) * get(i + 1, j + 2) * get(i + 2, j + 1)
        return f
    }

    private fun minor(
        i: Int,
        j: Int,
    ): Float = get(i + 1, j + 1) * get(i + 2, j + 2) - get(i + 1, j + 2) * get(i + 2, j + 1)

    private operator fun get(
        i: Int,
        j: Int,
    ): Float =
        when (i % 4) {
            0 -> {
                when (j % 4) {
                    0 -> this.m00
                    1 -> this.m01
                    2 -> this.m02
                    else -> this.m03
                }
            }

            1 -> {
                when (j % 4) {
                    0 -> this.m10
                    1 -> this.m11
                    2 -> this.m12
                    else -> this.m13
                }
            }

            2 -> {
                when (j % 4) {
                    0 -> this.m20
                    1 -> this.m21
                    2 -> this.m22
                    else -> this.m23
                }
            }

            else -> {
                when (j % 4) {
                    0 -> this.m30
                    1 -> this.m31
                    2 -> this.m32
                    else -> this.m33
                }
            }
        }

    private fun mulComponentWise(matrix4f: Matrix4f) {
        this.m00 *= matrix4f.m00
        this.m01 *= matrix4f.m01
        this.m02 *= matrix4f.m02
        this.m03 *= matrix4f.m03
        this.m10 *= matrix4f.m10
        this.m11 *= matrix4f.m11
        this.m12 *= matrix4f.m12
        this.m13 *= matrix4f.m13
        this.m20 *= matrix4f.m20
        this.m21 *= matrix4f.m21
        this.m22 *= matrix4f.m22
        this.m23 *= matrix4f.m23
        this.m30 *= matrix4f.m30
        this.m31 *= matrix4f.m31
        this.m32 *= matrix4f.m32
        this.m33 *= matrix4f.m33
    }

    private fun setInternal(
        i: Int,
        j: Int,
        f: Float,
    ) {
        when (i) {
            0 -> {
                when (j) {
                    0 -> this.m00 = f
                    1 -> this.m01 = f
                    2 -> this.m02 = f
                    else -> this.m03 = f
                }
            }

            1 -> {
                when (j) {
                    0 -> this.m10 = f
                    1 -> this.m11 = f
                    2 -> this.m12 = f
                    else -> this.m13 = f
                }
            }

            2 -> {
                when (j) {
                    0 -> this.m20 = f
                    1 -> this.m21 = f
                    2 -> this.m22 = f
                    else -> this.m23 = f
                }
            }

            else -> {
                when (j) {
                    0 -> this.m30 = f
                    1 -> this.m31 = f
                    2 -> this.m32 = f
                    else -> this.m33 = f
                }
            }
        }
    }

    fun loadIdentity() {
        this.m00 = 1.0f
        this.m01 = 0.0f
        this.m02 = 0.0f
        this.m03 = 0.0f
        this.m10 = 0.0f
        this.m11 = 1.0f
        this.m12 = 0.0f
        this.m13 = 0.0f
        this.m20 = 0.0f
        this.m21 = 0.0f
        this.m22 = 1.0f
        this.m23 = 0.0f
        this.m30 = 0.0f
        this.m31 = 0.0f
        this.m32 = 0.0f
        this.m33 = 1.0f
    }

    fun invert() {
        val f = adjugateAndDet()
        mul(f)
    }

    fun adjugateAndDet(): Float {
        val f = cofac(0, 0)
        val g = cofac(0, 1)
        val h = cofac(0, 2)
        val i = cofac(0, 3)
        val j = this.m00 * f + this.m01 * g + this.m02 * h + this.m03 * i
        this.m00 = f
        this.m10 = cofac(1, 0)
        this.m20 = cofac(2, 0)
        this.m30 = cofac(3, 0)
        this.m01 = g
        this.m11 = cofac(1, 1)
        this.m21 = cofac(2, 1)
        this.m31 = cofac(3, 1)
        this.m02 = h
        this.m12 = cofac(1, 2)
        this.m22 = cofac(2, 2)
        this.m32 = cofac(3, 2)
        this.m03 = i
        this.m13 = cofac(1, 3)
        this.m23 = cofac(2, 3)
        this.m33 = cofac(3, 3)
        return j
    }

    fun adjugateAndDetWith(matrix4f: Matrix4f): Float {
        this.m00 =
            matrix4f.m11 * matrix4f.m22 * matrix4f.m33 + matrix4f.m12 * matrix4f.m23 * matrix4f.m31 +
            matrix4f.m13 * matrix4f.m21 * matrix4f.m32 -
            matrix4f.m11 * matrix4f.m23 * matrix4f.m32 -
            matrix4f.m12 * matrix4f.m21 * matrix4f.m33 -
            matrix4f.m13 * matrix4f.m22 * matrix4f.m31
        this.m01 =
            -(
                matrix4f.m01 * matrix4f.m22 * matrix4f.m33 + matrix4f.m02 * matrix4f.m23 * matrix4f.m31 +
                    matrix4f.m03 * matrix4f.m21 * matrix4f.m32 -
                    matrix4f.m01 * matrix4f.m23 * matrix4f.m32 -
                    matrix4f.m02 * matrix4f.m21 * matrix4f.m33 -
                    matrix4f.m03 * matrix4f.m22 * matrix4f.m31
            )
        this.m02 =
            matrix4f.m01 * matrix4f.m12 * matrix4f.m33 + matrix4f.m02 * matrix4f.m13 * matrix4f.m31 +
            matrix4f.m03 * matrix4f.m11 * matrix4f.m32 -
            matrix4f.m01 * matrix4f.m13 * matrix4f.m32 -
            matrix4f.m02 * matrix4f.m11 * matrix4f.m33 -
            matrix4f.m03 * matrix4f.m12 * matrix4f.m31
        this.m03 =
            -(
                matrix4f.m01 * matrix4f.m12 * matrix4f.m23 + matrix4f.m02 * matrix4f.m13 * matrix4f.m21 +
                    matrix4f.m03 * matrix4f.m11 * matrix4f.m22 -
                    matrix4f.m01 * matrix4f.m13 * matrix4f.m22 -
                    matrix4f.m02 * matrix4f.m11 * matrix4f.m23 -
                    matrix4f.m03 * matrix4f.m12 * matrix4f.m21
            )
        this.m10 =
            -(
                matrix4f.m10 * matrix4f.m22 * matrix4f.m33 + matrix4f.m12 * matrix4f.m23 * matrix4f.m30 +
                    matrix4f.m13 * matrix4f.m20 * matrix4f.m32 -
                    matrix4f.m10 * matrix4f.m23 * matrix4f.m32 -
                    matrix4f.m12 * matrix4f.m20 * matrix4f.m33 -
                    matrix4f.m13 * matrix4f.m22 * matrix4f.m30
            )
        this.m11 =
            matrix4f.m00 * matrix4f.m22 * matrix4f.m33 + matrix4f.m02 * matrix4f.m23 * matrix4f.m30 +
            matrix4f.m03 * matrix4f.m20 * matrix4f.m32 -
            matrix4f.m00 * matrix4f.m23 * matrix4f.m32 -
            matrix4f.m02 * matrix4f.m20 * matrix4f.m33 -
            matrix4f.m03 * matrix4f.m22 * matrix4f.m30
        this.m12 =
            -(
                matrix4f.m00 * matrix4f.m12 * matrix4f.m33 + matrix4f.m02 * matrix4f.m13 * matrix4f.m30 +
                    matrix4f.m03 * matrix4f.m10 * matrix4f.m32 -
                    matrix4f.m00 * matrix4f.m13 * matrix4f.m32 -
                    matrix4f.m02 * matrix4f.m10 * matrix4f.m33 -
                    matrix4f.m03 * matrix4f.m12 * matrix4f.m30
            )
        this.m13 =
            matrix4f.m00 * matrix4f.m12 * matrix4f.m23 + matrix4f.m02 * matrix4f.m13 * matrix4f.m20 +
            matrix4f.m03 * matrix4f.m10 * matrix4f.m22 -
            matrix4f.m00 * matrix4f.m13 * matrix4f.m22 -
            matrix4f.m02 * matrix4f.m10 * matrix4f.m23 -
            matrix4f.m03 * matrix4f.m12 * matrix4f.m20
        this.m20 =
            matrix4f.m10 * matrix4f.m21 * matrix4f.m33 + matrix4f.m11 * matrix4f.m23 * matrix4f.m30 +
            matrix4f.m13 * matrix4f.m20 * matrix4f.m31 -
            matrix4f.m10 * matrix4f.m23 * matrix4f.m31 -
            matrix4f.m11 * matrix4f.m20 * matrix4f.m33 -
            matrix4f.m13 * matrix4f.m21 * matrix4f.m30
        this.m21 =
            -(
                matrix4f.m00 * matrix4f.m21 * matrix4f.m33 + matrix4f.m01 * matrix4f.m23 * matrix4f.m30 +
                    matrix4f.m03 * matrix4f.m20 * matrix4f.m31 -
                    matrix4f.m00 * matrix4f.m23 * matrix4f.m31 -
                    matrix4f.m01 * matrix4f.m20 * matrix4f.m33 -
                    matrix4f.m03 * matrix4f.m21 * matrix4f.m30
            )
        this.m22 =
            matrix4f.m00 * matrix4f.m11 * matrix4f.m33 + matrix4f.m01 * matrix4f.m13 * matrix4f.m30 +
            matrix4f.m03 * matrix4f.m10 * matrix4f.m31 -
            matrix4f.m00 * matrix4f.m13 * matrix4f.m31 -
            matrix4f.m01 * matrix4f.m10 * matrix4f.m33 -
            matrix4f.m03 * matrix4f.m11 * matrix4f.m30
        this.m23 =
            -(
                matrix4f.m00 * matrix4f.m11 * matrix4f.m23 + matrix4f.m01 * matrix4f.m13 * matrix4f.m20 +
                    matrix4f.m03 * matrix4f.m10 * matrix4f.m21 -
                    matrix4f.m00 * matrix4f.m13 * matrix4f.m21 -
                    matrix4f.m01 * matrix4f.m10 * matrix4f.m23 -
                    matrix4f.m03 * matrix4f.m11 * matrix4f.m20
            )
        this.m30 =
            -(
                matrix4f.m10 * matrix4f.m21 * matrix4f.m32 + matrix4f.m11 * matrix4f.m22 * matrix4f.m30 +
                    matrix4f.m12 * matrix4f.m20 * matrix4f.m31 -
                    matrix4f.m10 * matrix4f.m22 * matrix4f.m31 -
                    matrix4f.m11 * matrix4f.m20 * matrix4f.m32 -
                    matrix4f.m12 * matrix4f.m21 * matrix4f.m30
            )
        this.m31 =
            matrix4f.m00 * matrix4f.m21 * matrix4f.m32 + matrix4f.m01 * matrix4f.m22 * matrix4f.m30 +
            matrix4f.m02 * matrix4f.m20 * matrix4f.m31 -
            matrix4f.m00 * matrix4f.m22 * matrix4f.m31 -
            matrix4f.m01 * matrix4f.m20 * matrix4f.m32 -
            matrix4f.m02 * matrix4f.m21 * matrix4f.m30
        this.m32 =
            -(
                matrix4f.m00 * matrix4f.m11 * matrix4f.m32 + matrix4f.m01 * matrix4f.m12 * matrix4f.m30 +
                    matrix4f.m02 * matrix4f.m10 * matrix4f.m31 -
                    matrix4f.m00 * matrix4f.m12 * matrix4f.m31 -
                    matrix4f.m01 * matrix4f.m10 * matrix4f.m32 -
                    matrix4f.m02 * matrix4f.m11 * matrix4f.m30
            )
        this.m33 =
            matrix4f.m00 * matrix4f.m11 * matrix4f.m22 + matrix4f.m01 * matrix4f.m12 * matrix4f.m20 +
            matrix4f.m02 * matrix4f.m10 * matrix4f.m21 -
            matrix4f.m00 * matrix4f.m12 * matrix4f.m21 -
            matrix4f.m01 * matrix4f.m10 * matrix4f.m22 -
            matrix4f.m02 * matrix4f.m11 * matrix4f.m20
        return matrix4f.m00 * this.m00 + matrix4f.m01 * this.m10 + matrix4f.m02 * this.m20 + matrix4f.m03 * this.m30
    }

    fun mul(matrix4f: Matrix4f) {
        val f = this.m00 * matrix4f.m00 + this.m01 * matrix4f.m10 + this.m02 * matrix4f.m20 + this.m03 * matrix4f.m30
        val g = this.m00 * matrix4f.m01 + this.m01 * matrix4f.m11 + this.m02 * matrix4f.m21 + this.m03 * matrix4f.m31
        val h = this.m00 * matrix4f.m02 + this.m01 * matrix4f.m12 + this.m02 * matrix4f.m22 + this.m03 * matrix4f.m32
        val i = this.m00 * matrix4f.m03 + this.m01 * matrix4f.m13 + this.m02 * matrix4f.m23 + this.m03 * matrix4f.m33
        val j = this.m10 * matrix4f.m00 + this.m11 * matrix4f.m10 + this.m12 * matrix4f.m20 + this.m13 * matrix4f.m30
        val k = this.m10 * matrix4f.m01 + this.m11 * matrix4f.m11 + this.m12 * matrix4f.m21 + this.m13 * matrix4f.m31
        val l = this.m10 * matrix4f.m02 + this.m11 * matrix4f.m12 + this.m12 * matrix4f.m22 + this.m13 * matrix4f.m32
        val m = this.m10 * matrix4f.m03 + this.m11 * matrix4f.m13 + this.m12 * matrix4f.m23 + this.m13 * matrix4f.m33
        val n = this.m20 * matrix4f.m00 + this.m21 * matrix4f.m10 + this.m22 * matrix4f.m20 + this.m23 * matrix4f.m30
        val o = this.m20 * matrix4f.m01 + this.m21 * matrix4f.m11 + this.m22 * matrix4f.m21 + this.m23 * matrix4f.m31
        val p = this.m20 * matrix4f.m02 + this.m21 * matrix4f.m12 + this.m22 * matrix4f.m22 + this.m23 * matrix4f.m32
        val q = this.m20 * matrix4f.m03 + this.m21 * matrix4f.m13 + this.m22 * matrix4f.m23 + this.m23 * matrix4f.m33
        val r = this.m30 * matrix4f.m00 + this.m31 * matrix4f.m10 + this.m32 * matrix4f.m20 + this.m33 * matrix4f.m30
        val s = this.m30 * matrix4f.m01 + this.m31 * matrix4f.m11 + this.m32 * matrix4f.m21 + this.m33 * matrix4f.m31
        val t = this.m30 * matrix4f.m02 + this.m31 * matrix4f.m12 + this.m32 * matrix4f.m22 + this.m33 * matrix4f.m32
        val u = this.m30 * matrix4f.m03 + this.m31 * matrix4f.m13 + this.m32 * matrix4f.m23 + this.m33 * matrix4f.m33
        this.m00 = f
        this.m01 = g
        this.m02 = h
        this.m03 = i
        this.m10 = j
        this.m11 = k
        this.m12 = l
        this.m13 = m
        this.m20 = n
        this.m21 = o
        this.m22 = p
        this.m23 = q
        this.m30 = r
        this.m31 = s
        this.m32 = t
        this.m33 = u
    }

    fun setIdentity() {
        loadIdentity()
    }

    fun setTranslation(
        f: Float,
        g: Float,
        h: Float,
    ) {
        this.m03 = f
        this.m13 = g
        this.m23 = h
    }

    fun addTranslation(
        f: Float,
        g: Float,
        h: Float,
    ) {
        this.m03 += f
        this.m13 += g
        this.m23 += h
    }

    fun mulTranslation(
        f: Float,
        g: Float,
        h: Float,
    ) {
        this.m03 += this.m00 * f + this.m01 * g + this.m02 * h
        this.m13 += this.m10 * f + this.m11 * g + this.m12 * h
        this.m23 += this.m20 * f + this.m21 * g + this.m22 * h
        this.m33 += this.m30 * f + this.m31 * g + this.m32 * h
    }

    fun mulPose(quaternion: Quaternion) {
        val matrix3f = Matrix3f(quaternion)
        mul(matrix3f)
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

    fun mul(f: Float) {
        this.m00 *= f
        this.m01 *= f
        this.m02 *= f
        this.m03 *= f
        this.m10 *= f
        this.m11 *= f
        this.m12 *= f
        this.m13 *= f
        this.m20 *= f
        this.m21 *= f
        this.m22 *= f
        this.m23 *= f
        this.m30 *= f
        this.m31 *= f
        this.m32 *= f
        this.m33 *= f
    }

    fun translate(
        x: Double,
        y: Double,
        z: Double,
    ) {
        mulTranslation(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun multiply(vector4f: Vector4f) {
        val f = vector4f.x()
        val g = vector4f.y()
        val h = vector4f.z()
        val i = vector4f.w()
        vector4f.x = this.m00 * f + this.m01 * g + this.m02 * h + this.m03 * i
        vector4f.y = this.m10 * f + this.m11 * g + this.m12 * h + this.m13 * i
        vector4f.z = this.m20 * f + this.m21 * g + this.m22 * h + this.m23 * i
        vector4f.w = this.m30 * f + this.m31 * g + this.m32 * h + this.m33 * i
    }

    fun copy(): Matrix4f = Matrix4f(this)

    fun store(floatBuffer: FloatBuffer) {
        floatBuffer.put(0, this.m00)
        floatBuffer.put(1, this.m01)
        floatBuffer.put(2, this.m02)
        floatBuffer.put(3, this.m03)
        floatBuffer.put(4, this.m10)
        floatBuffer.put(5, this.m11)
        floatBuffer.put(6, this.m12)
        floatBuffer.put(7, this.m13)
        floatBuffer.put(8, this.m20)
        floatBuffer.put(9, this.m21)
        floatBuffer.put(10, this.m22)
        floatBuffer.put(11, this.m23)
        floatBuffer.put(12, this.m30)
        floatBuffer.put(13, this.m31)
        floatBuffer.put(14, this.m32)
        floatBuffer.put(15, this.m33)
    }

    fun load(floatBuffer: FloatBuffer) {
        this.m00 = floatBuffer[0]
        this.m01 = floatBuffer[1]
        this.m02 = floatBuffer[2]
        this.m03 = floatBuffer[3]
        this.m10 = floatBuffer[4]
        this.m11 = floatBuffer[5]
        this.m12 = floatBuffer[6]
        this.m13 = floatBuffer[7]
        this.m20 = floatBuffer[8]
        this.m21 = floatBuffer[9]
        this.m22 = floatBuffer[10]
        this.m23 = floatBuffer[11]
        this.m30 = floatBuffer[12]
        this.m31 = floatBuffer[13]
        this.m32 = floatBuffer[14]
        this.m33 = floatBuffer[15]
    }

    fun set(
        j: Int,
        k: Int,
        f: Float,
    ) {
        setInternal(j % 4, k % 4, f)
    }

    fun determinant(): Float {
        val f = cofac(0, 0)
        val g = cofac(0, 1)
        val h = cofac(0, 2)
        val i = cofac(0, 3)
        return this.m00 * f + this.m01 * g + this.m02 * h + this.m03 * i
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val matrix4f = other as Matrix4f
        return (
            java.lang.Float.compare(matrix4f.m00, m00) == 0 &&
                java.lang.Float.compare(matrix4f.m01, m01) == 0 &&
                java.lang.Float.compare(matrix4f.m02, m02) == 0 &&
                java.lang.Float.compare(matrix4f.m03, m03) == 0 &&
                java.lang.Float.compare(matrix4f.m10, m10) == 0 &&
                java.lang.Float.compare(matrix4f.m11, m11) == 0 &&
                java.lang.Float.compare(matrix4f.m12, m12) == 0 &&
                java.lang.Float.compare(matrix4f.m13, m13) == 0 &&
                java.lang.Float.compare(matrix4f.m20, m20) == 0 &&
                java.lang.Float.compare(matrix4f.m21, m21) == 0 &&
                java.lang.Float.compare(matrix4f.m22, m22) == 0 &&
                java.lang.Float.compare(matrix4f.m23, m23) == 0 &&
                java.lang.Float.compare(matrix4f.m30, m30) == 0 &&
                java.lang.Float.compare(matrix4f.m31, m31) == 0 &&
                java.lang.Float.compare(matrix4f.m32, m32) == 0 &&
                java.lang.Float.compare(matrix4f.m33, m33) == 0
        )
    }

    override fun hashCode(): Int {
        var i = if (this.m00 != 0.0f) java.lang.Float.floatToIntBits(this.m00) else 0
        i = 31 * i + if (this.m01 != 0.0f) java.lang.Float.floatToIntBits(this.m01) else 0
        i = 31 * i + if (this.m02 != 0.0f) java.lang.Float.floatToIntBits(this.m02) else 0
        i = 31 * i + if (this.m03 != 0.0f) java.lang.Float.floatToIntBits(this.m03) else 0
        i = 31 * i + if (this.m10 != 0.0f) java.lang.Float.floatToIntBits(this.m10) else 0
        i = 31 * i + if (this.m11 != 0.0f) java.lang.Float.floatToIntBits(this.m11) else 0
        i = 31 * i + if (this.m12 != 0.0f) java.lang.Float.floatToIntBits(this.m12) else 0
        i = 31 * i + if (this.m13 != 0.0f) java.lang.Float.floatToIntBits(this.m13) else 0
        i = 31 * i + if (this.m20 != 0.0f) java.lang.Float.floatToIntBits(this.m20) else 0
        i = 31 * i + if (this.m21 != 0.0f) java.lang.Float.floatToIntBits(this.m21) else 0
        i = 31 * i + if (this.m22 != 0.0f) java.lang.Float.floatToIntBits(this.m22) else 0
        i = 31 * i + if (this.m23 != 0.0f) java.lang.Float.floatToIntBits(this.m23) else 0
        i = 31 * i + if (this.m30 != 0.0f) java.lang.Float.floatToIntBits(this.m30) else 0
        i = 31 * i + if (this.m31 != 0.0f) java.lang.Float.floatToIntBits(this.m31) else 0
        i = 31 * i + if (this.m32 != 0.0f) java.lang.Float.floatToIntBits(this.m32) else 0
        i = 31 * i + if (this.m33 != 0.0f) java.lang.Float.floatToIntBits(this.m33) else 0
        return i
    }

    override fun toString(): String =
        (
            "Matrix4f:\n" + this.m00 + " " + this.m01 + " " + this.m02 + " " + this.m03 + "\n" + this.m10 + " " +
                this.m11 +
                " " +
                this.m12 +
                " " +
                this.m13 +
                "\n" +
                this.m20 +
                " " +
                this.m21 +
                " " +
                this.m22 +
                " " +
                this.m23 +
                "\n" +
                this.m30 +
                " " +
                this.m31 +
                " " +
                this.m32 +
                " " +
                this.m33 +
                "\n"
        )

    companion object {
        @JvmStatic
        fun perspective(
            f: Float,
            g: Float,
            h: Float,
            i: Float,
        ): Matrix4f {
            val matrix4f = Matrix4f()
            val j = (1.0 / Math.tan((f * 0.5).toDouble())).toFloat()
            matrix4f.m00 = j / g
            matrix4f.m11 = j
            matrix4f.m22 = (i + h) / (h - i)
            matrix4f.m32 = -1.0f
            matrix4f.m23 = 2.0f * i * h / (h - i)
            return matrix4f
        }

        private fun bufferIndex(
            i: Int,
            j: Int,
        ): Int = j * 4 + i

        @JvmStatic
        fun orthographic(
            f: Float,
            g: Float,
            h: Float,
            i: Float,
            j: Float,
            k: Float,
        ): Matrix4f {
            val matrix4f = Matrix4f()
            matrix4f.m00 = 2.0f / (g - f)
            matrix4f.m11 = 2.0f / (i - h)
            val l = k - j
            matrix4f.m22 = -2.0f / l
            matrix4f.m03 = -(g + f) / (g - f)
            matrix4f.m13 = -(i + h) / (i - h)
            matrix4f.m23 = -(k + j) / l
            matrix4f.m33 = 1.0f
            return matrix4f
        }

        @JvmStatic
        fun createTranslateMatrix(
            f: Float,
            g: Float,
            h: Float,
        ): Matrix4f {
            val matrix4f = Matrix4f()
            matrix4f.m00 = 1.0f
            matrix4f.m11 = 1.0f
            matrix4f.m22 = 1.0f
            matrix4f.m33 = 1.0f
            matrix4f.m03 = f
            matrix4f.m13 = g
            matrix4f.m23 = h
            return matrix4f
        }

        @JvmStatic
        fun createScaleMatrix(
            f: Float,
            g: Float,
            h: Float,
        ): Matrix4f {
            val matrix4f = Matrix4f()
            matrix4f.m00 = f
            matrix4f.m11 = g
            matrix4f.m22 = h
            matrix4f.m33 = 1.0f
            return matrix4f
        }

        @JvmStatic
        fun createRotateMatrix(quaternion: Quaternion): Matrix4f {
            val matrix4f = Matrix4f()
            val f = quaternion.i()
            val g = quaternion.j()
            val h = quaternion.k()
            val i = quaternion.r()
            val j = 2.0f * f * f
            val k = 2.0f * g * g
            val l = 2.0f * h * h
            matrix4f.m00 = 1.0f - k - l
            matrix4f.m11 = 1.0f - l - j
            matrix4f.m22 = 1.0f - j - k
            val m = f * g
            val n = g * h
            val o = h * f
            val p = f * i
            val q = g * i
            val r = h * i
            matrix4f.m10 = 2.0f * (m + r)
            matrix4f.m01 = 2.0f * (m - r)
            matrix4f.m20 = 2.0f * (o - q)
            matrix4f.m02 = 2.0f * (o + q)
            matrix4f.m21 = 2.0f * (n + p)
            matrix4f.m12 = 2.0f * (n - p)
            matrix4f.m33 = 1.0f
            return matrix4f
        }
    }
}
