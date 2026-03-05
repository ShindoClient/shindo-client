package me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout

import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.enums.UILayoutType
import me.miki.shindo.utils.ColorUtils
import kotlin.math.max

/**
 * Modules layout scene.
 *
 * It previews single and double column list dispositions for module cards.
 */
class LayoutModulesScene(parent: SettingsCategory) : LayoutCarouselScene(
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
        val pad = 10f
        val gap = 8f

        val contentX = x + pad
        val contentY = y + pad
        val contentWidth = width - pad * 2f
        val contentHeight = height - pad * 2f

        val columns = if (type == UILayoutType.MODULES_DOUBLE) 2 else 1
        val rows = 3

        val cardWidth = max(40f, (contentWidth - (columns - 1) * gap) / columns)
        val cardHeight = max(16f, (contentHeight - (rows - 1) * gap) / rows)

        nvg.drawRoundedRect(
            contentX,
            contentY,
            contentWidth,
            contentHeight,
            8f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 176)
        )

        var row = 0
        while (row < rows) {
            var col = 0
            while (col < columns) {
                val cardX = contentX + col * (cardWidth + gap)
                val cardY = contentY + row * (cardHeight + gap)
                drawModuleCard(nvg, palette, accent, cardX, cardY, cardWidth, cardHeight)
                col++
            }
            row++
        }
    }

    /**
     * Draws one module card sample.
     */
    private fun drawModuleCard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val cardColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230)
        val linePrimary = ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 220)
        val lineSecondary = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 190)
        val toggleColor = ColorUtils.applyAlpha(accent.getColor1(), 200)

        nvg.drawRoundedRect(x, y, width, height, 6f, cardColor)

        val lineY = y + max(4f, height * 0.28f)
        nvg.drawRoundedRect(x + 8f, lineY, max(10f, width - 28f), 2.6f, 1.3f, linePrimary)
        nvg.drawRoundedRect(x + 8f, lineY + 5f, max(8f, width - 36f), 2.3f, 1.2f, lineSecondary)

        val toggleSize = max(5f, height * 0.24f)
        val toggleX = x + width - toggleSize - 6f
        val toggleY = y + (height - toggleSize) / 2f
        nvg.drawRoundedRect(toggleX, toggleY, toggleSize, toggleSize, toggleSize / 2f, toggleColor)
    }
}
