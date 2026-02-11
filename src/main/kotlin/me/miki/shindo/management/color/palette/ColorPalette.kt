package me.miki.shindo.management.color.palette

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.Theme
import me.miki.shindo.ui.animation.value.ColorAnimation
import java.awt.Color

class ColorPalette {

    private val backgroundColorAnimations = Array(ColorType.values().size) { ColorAnimation() }
    private val fontColorAnimations = Array(ColorType.values().size) { ColorAnimation() }

    fun getBackgroundColor(type: ColorType, alpha: Int): Color =
        backgroundColorAnimations[type.getIndex()].getColor(getRawBackgroundColor(type, alpha))

    fun getBackgroundColor(type: ColorType): Color = getBackgroundColor(type, 255)

    private fun getRawBackgroundColor(type: ColorType, alpha: Int): Color {
        val theme = getTheme()
        return when (type) {
            ColorType.DARK -> theme.getDarkBackgroundColor(alpha)
            ColorType.MID -> theme.getMidBackgroundColor(alpha)
            ColorType.NORMAL -> theme.getNormalBackgroundColor(alpha)
        }
    }

    fun getFontColor(type: ColorType, alpha: Int): Color =
        fontColorAnimations[type.getIndex()].getColor(getRawFontColor(type, alpha))

    fun getFontColor(type: ColorType): Color = getFontColor(type, 255)

    private fun getRawFontColor(type: ColorType, alpha: Int): Color {
        val theme = getTheme()
        return when (type) {
            ColorType.DARK -> theme.getDarkFontColor(alpha)
            ColorType.MID -> theme.getMidFontColor(alpha)
            ColorType.NORMAL -> theme.getNormalFontColor(alpha)
        }
    }

    private fun getTheme(): Theme =
        Shindo.getInstance().colorManager.getTheme()

    fun getMaterialRed(alpha: Int): Color = Color(232, 38, 52, alpha)
    fun getMaterialYellow(alpha: Int): Color = Color(255, 255, 0, alpha)
    fun getMaterialRed(): Color = getMaterialRed(255)
    fun getMaterialYellow(): Color = getMaterialYellow(255)
}
