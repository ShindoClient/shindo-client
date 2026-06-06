package com.shindoclient.shindo.types

import kotlin.math.roundToInt

class Color
    @JvmOverloads
    constructor(
        private var r: Float,
        private var g: Float,
        private var b: Float,
        private var a: Float = 1f,
    ) {
        @JvmOverloads
        constructor(r: Int, g: Int, b: Int, a: Int = 255) : this(r / 255f, g / 255f, b / 255f, a / 255f)

        constructor(argb: Int) : this(argb shr 16 and 0xFF, argb shr 8 and 0xFF, argb and 0xFF, argb shr 24 and 0xFF)

        fun setAlpha(alpha: Float) {
            a = alpha
        }

        fun setAlpha(alpha: Int) {
            a = alpha / 255f
        }

        fun setR(r: Float) {
            this.r = r
        }

        fun setG(g: Float) {
            this.g = g
        }

        fun setB(b: Float) {
            this.b = b
        }

        fun toARGB(): Int {
            val a = (a * 255).roundToInt()
            val r = (r * 255).roundToInt()
            val g = (g * 255).roundToInt()
            val b = (b * 255).roundToInt()
            return a shl 24 or (r shl 16) or (g shl 8) or b
        }

        companion object {
            fun Interpolate(
                from: Color,
                to: Color,
                p: Float,
                out: Color,
            ): Color {
                out.r = from.r + (to.r - from.r) * p
                out.g = from.g + (to.g - from.g) * p
                out.b = from.b + (to.b - from.b) * p
                out.a = from.a + (to.a - from.a) * p
                return out
            }
        }
    }
