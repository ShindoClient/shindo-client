package me.miki.shindo.ui.components.v2.chips

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

object CategoryChipRenderer {
    const val CHIP_HEIGHT = 22f
    const val CHIP_HORIZONTAL_PADDING = 12f
    private const val CHIP_RADIUS = 6f

    @JvmStatic
    fun computeWidth(
        nvg: NanoVGManager,
        label: String?,
        icon: String?,
    ): Float {
        var iconWidth = 0f
        if (!icon.isNullOrEmpty()) {
            iconWidth = nvg.getTextWidth(icon, 12f, Fonts.LEGACYICON) + 6f
        }
        val textWidth = if (label == null) 0f else nvg.getTextWidth(label, 9.5f, Fonts.MEDIUM)
        return CHIP_HORIZONTAL_PADDING * 2f + iconWidth + textWidth
    }

    @JvmStatic
    fun drawChip(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        label: String?,
        icon: String?,
        active: Boolean,
        hovered: Boolean,
    ) {
        val background =
            ColorUtils.applyAlpha(
                palette.getBackgroundColor(ColorType.DARK),
                if (hovered || active) 235 else 205,
            )

        if (active) {
            nvg.drawGradientRoundedRect(
                x,
                y,
                width,
                CHIP_HEIGHT,
                CHIP_RADIUS,
                ColorUtils.applyAlpha(accent.getColor1(), 210),
                ColorUtils.applyAlpha(accent.getColor2(), 210),
            )
        } else {
            nvg.drawRoundedRect(x, y, width, CHIP_HEIGHT, CHIP_RADIUS, background)
        }

        var textX = x + CHIP_HORIZONTAL_PADDING
        val textY = y + CHIP_HEIGHT / 2f - 1f
        val textColor: Color = if (active) Color.WHITE else palette.getFontColor(ColorType.NORMAL)

        if (!icon.isNullOrEmpty()) {
            nvg.drawText(icon, textX, textY - 4f, textColor, 12f, Fonts.LEGACYICON)
            textX += nvg.getTextWidth(icon, 12f, Fonts.LEGACYICON) + 4f
        }

        if (!label.isNullOrEmpty()) {
            nvg.drawText(label, textX, textY - 2f, textColor, 9.5f, Fonts.MEDIUM)
        }
    }
}
