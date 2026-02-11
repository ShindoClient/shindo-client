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
import kotlin.math.max

class LayoutSettingsScene(parent: SettingsCategory) :
        LayoutCarouselScene(
                parent,
                UILayoutArea.SETTINGS,
                TranslateText.SETTINGS,
                TranslateText.SETTINGS_LAYOUT_DESCRIPTION,
                LegacyIcon.SETTINGS
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
        val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 176)
        val cardOuter = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 212)
        val cardInner = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220)
        val lineColor = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 212)
        val accentColor = ColorUtils.applyAlpha(accent.getColor1(), 198)

        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, base)

        val columns = if (type == UILayoutType.SETTINGS_DOUBLE) 2 else 1
        val rows = 3
        val padding = 12f
        val colGap = 8f
        val rowGap = 8f
        val footerHeight = 34f
        val headerHeight = 5f
        val headerSpacing = 3f
        val availableHeight = max(42f, height - padding * 2f - footerHeight)
        val cellHeight = max(24f, (availableHeight - (rows - 1) * rowGap) / rows)
        val cardHeight = max(14f, cellHeight - headerHeight - headerSpacing)
        val columnWidth = (width - padding * 2f - (columns - 1) * colGap) / columns

        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val cardX = x + padding + column * (columnWidth + colGap)
                val headerY = y + padding + row * (cellHeight + rowGap)
                val cardY = headerY + headerHeight + headerSpacing

                nvg.drawRoundedRect(cardX, headerY, 7f, headerHeight, 2f, accentColor)
                nvg.drawRoundedRect(cardX + 9f, headerY, max(12f, columnWidth - 9f), headerHeight, 2f, lineColor)

                nvg.drawRoundedRect(cardX, cardY, columnWidth, cardHeight, 7f, cardOuter)
                nvg.drawRoundedRect(cardX + 1f, cardY + 1f, columnWidth - 2f, cardHeight - 2f, 6f, cardInner)

                val lineX = cardX + 8f
                val lineY1 = cardY + 5f
                val lineY2 = cardY + 11f
                nvg.drawRoundedRect(lineX, lineY1, 5f, 2.5f, 1.2f, accentColor)
                nvg.drawRoundedRect(lineX + 8f, lineY1, max(14f, columnWidth - 22f), 2.5f, 1.2f, lineColor)
                nvg.drawRoundedRect(lineX, lineY2, max(18f, columnWidth - 18f), 2.4f, 1.2f, ColorUtils.applyAlpha(lineColor, 185))
            }
        }

        val footerY = y + height - footerHeight - 10f
        nvg.drawRoundedRect(
                x + padding,
                footerY,
                width - padding * 2f,
                footerHeight,
                7f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 170)
        )
        nvg.drawText(type.getTitle(), x + padding + 10f, footerY + 8f, palette.getFontColor(ColorType.DARK), 10f, Fonts.MEDIUM)
        nvg.drawText(
                type.getDescription(),
                x + padding + 10f,
                footerY + 20f,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 190),
                8.2f,
                Fonts.REGULAR
        )
    }
}
