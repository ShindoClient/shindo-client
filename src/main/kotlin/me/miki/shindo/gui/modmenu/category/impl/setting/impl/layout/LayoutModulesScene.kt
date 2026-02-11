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

class LayoutModulesScene(parent: SettingsCategory) :
        LayoutCarouselScene(
                parent,
                UILayoutArea.MODULES,
                TranslateText.SETTINGS_LAYOUT_SECTION_MODULE,
                TranslateText.SETTINGS_LAYOUT_MODULE_DESCRIPTION,
                LegacyIcon.LIST
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
        val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 178)
        val cardColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 232)
        val line1Color = ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 218)
        val line2Color = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 196)
        val toggleColor = ColorUtils.applyAlpha(accent.getColor1(), 198)

        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, base)

        val columns = if (type == UILayoutType.MODULES_DOUBLE) 2 else 1
        val rows = 3
        val padding = 12f
        val rowGap = 8f
        val colGap = 8f
        val footerHeight = 34f
        val availableHeight = max(40f, height - padding * 2f - footerHeight)
        val cardHeight = max(20f, (availableHeight - (rows - 1) * rowGap) / rows)
        val cardWidth = (width - padding * 2f - (columns - 1) * colGap) / columns

        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val cardX = x + padding + column * (cardWidth + colGap)
                val cardY = y + padding + row * (cardHeight + rowGap)
                nvg.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, 6f, cardColor)

                val lineX = cardX + 8f
                val line1Y = cardY + max(4f, cardHeight * 0.28f)
                val line2Y = cardY + max(8f, cardHeight * 0.6f)
                val line1Width = max(10f, cardWidth - 26f)
                val line2Width = max(8f, cardWidth - 34f)
                val lineHeight = max(2f, cardHeight * 0.12f)

                nvg.drawRoundedRect(lineX, line1Y, line1Width, lineHeight, lineHeight / 2f, line1Color)
                nvg.drawRoundedRect(lineX, line2Y, line2Width, lineHeight, lineHeight / 2f, line2Color)

                val toggleSize = max(5f, cardHeight * 0.24f)
                val toggleX = cardX + cardWidth - toggleSize - 6f
                val toggleY = cardY + (cardHeight - toggleSize) / 2f
                nvg.drawRoundedRect(toggleX, toggleY, toggleSize, toggleSize, toggleSize / 2f, toggleColor)
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
