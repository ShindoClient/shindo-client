@file:JvmName("ColorExtensions")

package me.miki.extensions.management.color

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.Theme
import me.miki.shindo.utils.ColorUtils

private fun Float.toAlphaInt(): Int {
    return (coerceIn(0f, 1f) * 255).toInt()
}

fun AccentColor.toColorInt(): Int {
    return getColor1().rgb
}

fun AccentColor.applyAlpha(alpha: Float): Int {
    return ColorUtils.applyAlpha(getColor1(), alpha.toAlphaInt()).rgb
}

fun Int.applyAlpha(alpha: Float): Int {
    return ColorUtils.applyAlpha(this, alpha.toAlphaInt())
}

fun Int.interpolateTo(target: Int, progress: Float): Int {
    val clamped = progress.coerceIn(0f, 1f).toDouble()
    val fromColor = ColorUtils.getColorByInt(this)
    val toColor = ColorUtils.getColorByInt(target)
    return ColorUtils.interpolateColor(fromColor, toColor, clamped).rgb
}

fun ColorManager.getOrDefault(key: String, fallback: Int): Int {
    return runCatching { getColorByName(key).getColor1().rgb }.getOrDefault(fallback)
}

val Theme.isDark: Boolean
    get() = ColorUtils.getBrightness(getDarkBackgroundColor()) < 0.5f
