package me.miki.shindo.ui.components.v2.style

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

object CompStyleResolver {

    enum class CompControlVariant {
        PRIMARY, SECONDARY, GHOST, SUCCESS, DANGER
    }

    fun resolveControlBase(variant: CompControlVariant, palette: ColorPalette, accent: AccentColor): Color {
        val base = when (variant) {
            CompControlVariant.PRIMARY -> ColorUtils.applyAlpha(
                ColorUtils.interpolateColor(palette.getBackgroundColor(ColorType.MID), accent.getColor1(), 0.08),
                190
            )
            CompControlVariant.SECONDARY -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210)
            CompControlVariant.GHOST -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 108)
            CompControlVariant.SUCCESS -> Color(58, 182, 104, 210)
            CompControlVariant.DANGER -> Color(208, 78, 94, 210)
        }
        return base
    }

    fun resolveControlHover(variant: CompControlVariant, palette: ColorPalette, accent: AccentColor): Color {
        return when (variant) {
            CompControlVariant.PRIMARY -> ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 225)
            CompControlVariant.SECONDARY -> ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 165)
            CompControlVariant.GHOST -> ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 145)
            CompControlVariant.SUCCESS -> Color(72, 196, 116, 220)
            CompControlVariant.DANGER -> Color(224, 92, 108, 220)
        }
    }

    fun resolveControlText(variant: CompControlVariant, palette: ColorPalette): Color {
        return when (variant) {
            CompControlVariant.GHOST -> palette.getFontColor(ColorType.DARK)
            else -> Color.WHITE
        }
    }
}