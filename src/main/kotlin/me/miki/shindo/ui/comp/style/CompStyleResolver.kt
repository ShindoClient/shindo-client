package me.miki.shindo.ui.comp.style

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

object CompStyleResolver {

    fun resolveSurfaceBackground(variant: CompSurfaceVariant, palette: ColorPalette, accent: AccentColor): Color {
        val base = when (variant) {
            CompSurfaceVariant.CANVAS -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 220)
            CompSurfaceVariant.PANEL -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210)
            CompSurfaceVariant.CARD -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210)
            CompSurfaceVariant.OVERLAY -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 205)
            CompSurfaceVariant.TRANSPARENT -> Color(0, 0, 0, 0)
        }
        return VisualPresetModificationManager.applySurfaceBackground(activePreset(), variant, base, palette, accent)
    }


    fun resolveControlBase(variant: CompControlVariant, palette: ColorPalette, accent: AccentColor): Color {
        val base = when (variant) {
            CompControlVariant.PRIMARY -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 190)
            CompControlVariant.SECONDARY -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210)
            CompControlVariant.GHOST -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 108)
            CompControlVariant.SUCCESS -> Color(58, 182, 104, 210)
            CompControlVariant.DANGER -> Color(208, 78, 94, 210)
        }
        return VisualPresetModificationManager.applyControlBase(activePreset(), variant, base, palette, accent)
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

    private fun activePreset(): InternalSettingsMod.VisualPreset {
        return try {
            val preset = InternalSettingsMod.instance.getVisualPreset()
            if (preset == InternalSettingsMod.VisualPreset.CLASSIC) {
                InternalSettingsMod.VisualPreset.MODERN
            } else {
                preset
            }
        } catch (ignored: Throwable) {
            InternalSettingsMod.VisualPreset.MODERN
        }
    }
}
