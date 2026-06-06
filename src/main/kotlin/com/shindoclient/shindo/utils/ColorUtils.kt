package com.shindoclient.shindo.utils

import com.shindoclient.shindo.utils.MathUtils.interpolateFloat
import com.shindoclient.shindo.utils.MathUtils.interpolateInt
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

object ColorUtils {
    @JvmStatic
    fun getRainbow(
        index: Int,
        speed: Double,
        alpha: Int,
    ): Color {
        val angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        val hue = angle / 360f
        val color = Color.HSBtoRGB(hue, 1f, 1f)
        val base = Color(color)
        return Color(base.red, base.green, base.blue, alpha)
    }

    @JvmStatic
    fun interpolateColors(
        speed: Int,
        index: Int,
        start: Color,
        end: Color,
    ): Color {
        var angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        angle = (if (angle >= 180) 360 - angle else angle) * 2
        return interpolateColorHue(start, end, angle / 360f)
    }

    private fun interpolateColorHue(
        color1: Color,
        color2: Color,
        amount: Float,
    ): Color {
        var amount = amount
        amount = 1f.coerceAtMost(0f.coerceAtLeast(amount))
        val color1HSB = Color.RGBtoHSB(color1.red, color1.green, color1.blue, null)
        val color2HSB = Color.RGBtoHSB(color2.red, color2.green, color2.blue, null)
        val resultColor =
            Color.getHSBColor(
                interpolateFloat(color1HSB[0], color2HSB[0], amount.toDouble()),
                interpolateFloat(
                    color1HSB[1],
                    color2HSB[1],
                    amount.toDouble(),
                ),
                interpolateFloat(color1HSB[2], color2HSB[2], amount.toDouble()),
            )
        return Color(
            resultColor.red,
            resultColor.green,
            resultColor.blue,
            interpolateInt(color1.alpha, color2.alpha, amount.toDouble()),
        )
    }

    @JvmStatic
    fun interpolateColor(
        from: Color,
        to: Color,
        delta: Double,
    ): Color {
        val red = interpolateInt(from.red, to.red, delta)
        val green = interpolateInt(from.green, to.green, delta)
        val blue = interpolateInt(from.blue, to.blue, delta)
        val alpha = interpolateInt(from.alpha, to.alpha, delta)
        return Color(red, green, blue, alpha)
    }

    @JvmStatic
    fun transitionColor(
        from: Color,
        to: Color,
        state: Boolean,
    ): Color = interpolateColor(from, to, if (state) 1.0 else 0.0)

    @JvmStatic
    fun transitionColor(
        from: Color,
        to: Color,
        state: Boolean,
        speed: Int,
        timer: TimerUtils,
    ): Color {
        val elapsed = timer.elapsedTime.toDouble() / speed.coerceAtLeast(1)
        val progress = elapsed.coerceIn(0.0, 1.0)
        return interpolateColor(from, to, if (state) progress else 1.0 - progress)
    }

    @JvmStatic
    fun getHue(color: Color): Float = rgbToHsb(color)[0]

    @JvmStatic
    fun getSaturation(color: Color): Float = rgbToHsb(color)[1]

    @JvmStatic
    fun getBrightness(color: Color): Float = rgbToHsb(color)[2]

    private fun rgbToHsb(color: Color): FloatArray {
        val hsv = FloatArray(3)
        Color.RGBtoHSB(color.red, color.green, color.blue, hsv)
        return hsv
    }

    @JvmStatic
    fun removeColorCode(text: String): String =
        text
            .replace("\\u00a7" + "1".toRegex(), "")
            .replace("\\u00a7" + "2".toRegex(), "")
            .replace("\\u00a7" + "3".toRegex(), "")
            .replace("\\u00a7" + "4".toRegex(), "")
            .replace("\\u00a7" + "5".toRegex(), "")
            .replace("\\u00a7" + "6".toRegex(), "")
            .replace("\\u00a7" + "7".toRegex(), "")
            .replace("\\u00a7" + "8".toRegex(), "")
            .replace("\\u00a7" + "9".toRegex(), "")
            .replace("\\u00a7" + "a".toRegex(), "")
            .replace("\\u00a7" + "b".toRegex(), "")
            .replace("\\u00a7" + "c".toRegex(), "")
            .replace("\\u00a7" + "d".toRegex(), "")
            .replace("\\u00a7" + "e".toRegex(), "")
            .replace("\\u00a7" + "f".toRegex(), "")
            .replace("\\u00a7" + "g".toRegex(), "")
            .replace("\\u00a7" + "k".toRegex(), "")
            .replace("\\u00a7" + "l".toRegex(), "")
            .replace("\\u00a7" + "m".toRegex(), "")
            .replace("\\u00a7" + "n".toRegex(), "")
            .replace("\\u00a7" + "o".toRegex(), "")
            .replace("\\u00a7" + "r".toRegex(), "")

    @JvmStatic
    fun setColor(
        color: Int,
        alpha: Float,
    ) {
        val r = (color shr 16 and 255) / 255.0f
        val g = (color shr 8 and 255) / 255.0f
        val b = (color and 255) / 255.0f
        GlStateManager.color(r, g, b, alpha)
    }

    @JvmStatic
    fun getColorByInt(color: Int): Color {
        val r = (color shr 16 and 255) / 255.0f
        val g = (color shr 8 and 255) / 255.0f
        val b = (color and 255) / 255.0f
        val a = (color shr 24 and 255) / 255.0f
        return Color(r, g, b, a)
    }

    @JvmStatic
    fun setColor(color: Int) {
        setColor(color, (color shr 24 and 255) / 255.0f)
    }

    @JvmStatic
    fun resetColor() {
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    @JvmStatic
    fun applyAlpha(
        color: Color,
        alpha: Int,
    ): Color {
        val r = color.red
        val g = color.green
        val b = color.blue
        return Color(r, g, b, alpha)
    }

    @JvmStatic
    fun applyAlpha(
        color: Int,
        alpha: Int,
    ): Int {
        val r = color shr 16 and 255
        val g = color shr 8 and 255
        val b = color and 255
        return alpha shl 24 or (r shl 16) or (g shl 8) or b
    }

    @JvmStatic
    fun lighten(
        color: Color,
        amount: Float,
    ): Color {
        val clamped = max(0f, min(1f, amount))
        val r = color.red
        val g = color.green
        val b = color.blue
        val a = color.alpha

        val newR = min(255f, r + ((255 - r) * clamped)).toInt()
        val newG = min(255f, g + ((255 - g) * clamped)).toInt()
        val newB = min(255f, b + ((255 - b) * clamped)).toInt()

        return Color(newR, newG, newB, a)
    }

    @JvmStatic
    fun darken(
        color: Color,
        amount: Float,
    ): Color {
        val clamped = max(0f, min(1f, amount))
        val r = color.red
        val g = color.green
        val b = color.blue
        val a = color.alpha

        val newR = max(0f, r - (r * clamped)).toInt()
        val newG = max(0f, g - (g * clamped)).toInt()
        val newB = max(0f, b - (b * clamped)).toInt()

        return Color(newR, newG, newB, a)
    }

    @JvmStatic
    fun getAlphaByInt(color: Int): Float = (color shr 24 and 255) / 255.0f

    @JvmStatic
    fun color(color: Int) {
        val alpha = (color shr 24 and 255) / 255f
        val red = (color shr 16 and 255) / 255f
        val green = (color shr 8 and 255) / 255f
        val blue = (color and 255) / 255f
        GlStateManager.color(red, green, blue, alpha)
    }
}
