package me.miki.shindo.gui.modmenu.render

import me.miki.shindo.gui.modmenu.style.ModMenuListCardStyle
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

/**
 * Shared card renderer for ModMenu list categories (Modules/Addons).
 *
 * Category-specific text and icon content remain in each category; this helper
 * only draws common surfaces and controls.
 */
object ModMenuListCardRenderer {

    fun drawCardShell(
        nvg: NanoVGManager,
        palette: ColorPalette,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        hoverProgress: Float
    ) {
        nvg.drawShadow(x, y, width, height, 8f, 7)
        nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            ModMenuListCardStyle.CARD_RADIUS,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 220)
        )
        nvg.drawOutlineRoundedRect(
            x,
            y,
            width,
            height,
            ModMenuListCardStyle.CARD_RADIUS,
            1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210)
        )
    }

    fun drawSettingsButton(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        size: Float,
        hoverProgress: Float
    ) {
        nvg.drawRoundedRect(
            x,
            y,
            size,
            size,
            ModMenuListCardStyle.SETTINGS_RADIUS,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 180)
        )
        nvg.drawCenteredText(
            LegacyIcon.SETTINGS,
            x + (size / 2f),
            y + 2f,
            ColorUtils.interpolateColor(palette.getFontColor(ColorType.DARK),accent.getInterpolateColor(),hoverProgress.toDouble()),
            ModMenuListCardStyle.SETTINGS_ICON_SIZE,
            Fonts.LEGACYICON
        )
    }

    fun drawToggle(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        progress: Float
    ) {
        val toggleRadius = height / 2f
        val toggleBase = ColorUtils.applyAlpha(
            palette.getBackgroundColor(ColorType.NORMAL),
            ModMenuListCardStyle.TOGGLE_BASE_ALPHA
        )
        nvg.drawRoundedRect(x, y, width, height, toggleRadius, toggleBase)

        if (progress > 0f) {
            nvg.drawGradientRoundedRect(
                x,
                y,
                width,
                height,
                toggleRadius,
                ColorUtils.applyAlpha(accent.getColor1(), (progress * 255).toInt()),
                ColorUtils.applyAlpha(accent.getColor2(), (progress * 255).toInt())
            )
        }

        val knobSize = height - (ModMenuListCardStyle.TOGGLE_KNOB_INSET * 2f)
        val knobX = x + ModMenuListCardStyle.TOGGLE_KNOB_INSET +
                progress * (width - knobSize - (ModMenuListCardStyle.TOGGLE_KNOB_INSET * 2f))
        val knobY = y + ModMenuListCardStyle.TOGGLE_KNOB_INSET
        nvg.drawRoundedRect(knobX, knobY, knobSize, knobSize, knobSize / 2f, Color.WHITE)
    }
}
