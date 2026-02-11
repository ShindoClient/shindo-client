package me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout

import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.enums.UILayoutType
import me.miki.shindo.utils.ColorUtils
import java.awt.Color
import kotlin.math.max

class LayoutVisualScene(parent: SettingsCategory) :
        LayoutCarouselScene(
                parent,
                UILayoutArea.VISUAL,
                TranslateText.PRESETS,
                TranslateText.APPEARANCE_DESCRIPTION,
                LegacyIcon.COLOUR
        ) {

    override fun drawCarouselPreset(
            nvg: NanoVGManager,
            palette: ColorPalette,
            accent: AccentColor,
            type: UILayoutType,
            x: Float,
            y: Float,
            width: Float,
            height: Float
    ) {
        val style = resolveStyle(type, palette, accent)

        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, style.background)

        val topPadding = 12f
        val contentPadding = 12f
        val cardGap = 10f
        val titleRowHeight = 18f
        val footerHeight = 34f
        val contentStartY = y + topPadding + titleRowHeight
        val availableCardsHeight = height - (contentStartY - y) - footerHeight - contentPadding - cardGap
        val cardHeight = max(38f, availableCardsHeight / 2f)
        val cardWidth = width - contentPadding * 2f

        nvg.drawRoundedRect(
                x + contentPadding,
                y + topPadding,
                max(46f, width * 0.26f),
                8f,
                3f,
                ColorUtils.applyAlpha(style.textPrimary, 190)
        )
        nvg.drawRoundedRect(
                x + contentPadding + max(52f, width * 0.3f),
                y + topPadding + 1.5f,
                max(56f, width * 0.42f),
                5.5f,
                2f,
                ColorUtils.applyAlpha(style.textSecondary, 170)
        )

        for (row in 0..1) {
            val cardY = contentStartY + row * (cardHeight + cardGap)
            nvg.drawRoundedRect(x + contentPadding, cardY, cardWidth, cardHeight, 8f, style.card)

            val accentWidth = max(26f, cardWidth * style.accentWidthFactor)
            nvg.drawGradientRoundedRect(
                    x + contentPadding + 8f,
                    cardY + 8f,
                    accentWidth,
                    max(8f, cardHeight - 16f),
                    6f,
                    style.accentA,
                    style.accentB
            )
            nvg.drawRoundedRect(
                    x + contentPadding + accentWidth + 16f,
                    cardY + 8f,
                    max(36f, cardWidth * 0.5f),
                    6f,
                    2f,
                    style.textPrimary
            )
            nvg.drawRoundedRect(
                    x + contentPadding + accentWidth + 16f,
                    cardY + 18f,
                    max(30f, cardWidth * 0.4f),
                    5f,
                    2f,
                    style.textSecondary
            )
        }

        val footerY = y + height - footerHeight - 10f
        nvg.drawRoundedRect(
                x + contentPadding,
                footerY,
                cardWidth,
                footerHeight,
                7f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 170)
        )
        nvg.drawText(type.getTitle(), x + contentPadding + 10f, footerY + 8f, palette.getFontColor(ColorType.DARK), 10f, Fonts.MEDIUM)
        nvg.drawText(
                type.getDescription(),
                x + contentPadding + 10f,
                footerY + 20f,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 190),
                8.2f,
                Fonts.REGULAR
        )
    }

    private fun resolveStyle(type: UILayoutType, palette: ColorPalette, accent: AccentColor): VisualStyle {
        return when (type) {
            UILayoutType.VISUAL_LIGHT -> VisualStyle(
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 225),
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 245),
                    ColorUtils.applyAlpha(accent.getColor1(), 165),
                    ColorUtils.applyAlpha(accent.getColor2(), 142),
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 220),
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 192),
                    0.2f
            )

            UILayoutType.VISUAL_DARK -> VisualStyle(
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 235),
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 245),
                    ColorUtils.applyAlpha(accent.getColor2(), 188),
                    ColorUtils.applyAlpha(accent.getColor1(), 168),
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 228),
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200),
                    0.24f
            )

            else -> VisualStyle(
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 225),
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 238),
                    ColorUtils.applyAlpha(accent.getColor1(), 200),
                    ColorUtils.applyAlpha(accent.getColor2(), 182),
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 225),
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 198),
                    0.32f
            )
        }
    }

    private data class VisualStyle(
            val background: Color,
            val card: Color,
            val accentA: Color,
            val accentB: Color,
            val textPrimary: Color,
            val textSecondary: Color,
            val accentWidthFactor: Float
    )
}
