package me.miki.shindo.gui.modmenu.v2.render

import me.miki.shindo.gui.modmenu.v2.style.ModMenuListCardStyle
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

object ModMenuListCardRenderer {

    fun drawCardShell(
        nvg: NanoVGManager,
        palette: ColorPalette,
        x: Float, y: Float,
        width: Float, height: Float,
        hoverProgress: Float
    ) {
        nvg.drawShadow(x, y, width, height, 8f, 7)
        nvg.drawRoundedRect(
            x, y, width, height,
            ModMenuListCardStyle.CARD_RADIUS,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 220)
        )
        nvg.drawOutlineRoundedRect(
            x, y, width, height,
            ModMenuListCardStyle.CARD_RADIUS, 1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210)
        )
    }

    fun drawSettingsButton(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float, y: Float,
        size: Float,
        hoverProgress: Float
    ) {
        nvg.drawRoundedRect(
            x, y, size, size,
            ModMenuListCardStyle.SETTINGS_RADIUS,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 180)
        )
        nvg.drawCenteredText(
            LegacyIcon.SETTINGS,
            x + size / 2f, y + 2f,
            ColorUtils.interpolateColor(
                palette.getFontColor(ColorType.DARK),
                accent.getInterpolateColor(),
                hoverProgress.toDouble()
            ),
            ModMenuListCardStyle.SETTINGS_ICON_SIZE,
            Fonts.LEGACYICON
        )
    }

    fun drawToggle(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float, y: Float,
        width: Float, height: Float,
        progress: Float
    ) {
        val radius = height / 2f
        val knobInset = ModMenuListCardStyle.TOGGLE_KNOB_INSET

        nvg.drawRoundedRect(
            x, y, width, height, radius,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), ModMenuListCardStyle.TOGGLE_BASE_ALPHA)
        )

        if (progress > 0f) {
            val alpha = (progress * 255).toInt()
            nvg.drawGradientRoundedRect(
                x, y, width, height, radius,
                ColorUtils.applyAlpha(accent.getColor1(), alpha),
                ColorUtils.applyAlpha(accent.getColor2(), alpha)
            )
        }

        val knobSize = height - knobInset * 2f
        val knobX = x + knobInset + progress * (width - knobSize - knobInset * 2f)
        nvg.drawRoundedRect(knobX, y + knobInset, knobSize, knobSize, knobSize / 2f, Color.WHITE)
    }
}