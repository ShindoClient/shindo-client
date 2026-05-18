package me.miki.shindo.gui.modmenu.v2.category.impl.setting.impl.layout

import me.miki.shindo.gui.modmenu.v2.category.impl.SettingsCategory
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.enums.UILayoutType
import me.miki.shindo.utils.ColorUtils
import java.awt.Color
import kotlin.math.max

/**
 * Settings layout scene.
 *
 * It previews the 3 settings panel dispositions:
 * - Single column
 * - Double column
 * - Adaptive (staggered + full-width heavy component)
 */
class LayoutSettingsScene(
    parent: SettingsCategory,
) : LayoutCarouselScene(
        parent,
        UILayoutArea.SETTINGS,
        TranslateText.SETTINGS,
        TranslateText.SETTINGS_LAYOUT_DESCRIPTION,
        LegacyIcon.SETTINGS,
    ) {
    override fun drawCarouselPreset(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        type: UILayoutType,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        val pad = 10f
        val gap = 7f
        val headerHeight = 3.3f

        val contentX = x + pad
        val contentY = y + pad
        val contentWidth = width - pad * 2f
        val contentHeight = height - pad * 2f

        val frameColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 178)
        val blockOuter = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 215)
        val blockInner = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 228)
        val linePrimary = ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 216)
        val lineSecondary = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 188)
        val accentColor = ColorUtils.applyAlpha(accent.getColor1(), 196)

        nvg.drawRoundedRect(contentX, contentY, contentWidth, contentHeight, 8f, frameColor)

        when (type) {
            UILayoutType.SETTINGS_SINGLE -> {
                val rows = 3
                val itemHeight = max(18f, (contentHeight - gap * (rows - 1)) / rows)
                var row = 0
                while (row < rows) {
                    drawSettingBlock(
                        nvg,
                        contentX,
                        contentY + row * (itemHeight + gap),
                        contentWidth,
                        itemHeight,
                        headerHeight,
                        blockOuter,
                        blockInner,
                        accentColor,
                        linePrimary,
                        lineSecondary,
                    )
                    row++
                }
            }

            UILayoutType.SETTINGS_DOUBLE -> {
                val rows = 3
                val columns = 2
                val itemHeight = max(18f, (contentHeight - gap * (rows - 1)) / rows)
                val columnWidth = max(40f, (contentWidth - gap * (columns - 1)) / columns)
                var row = 0
                while (row < rows) {
                    var col = 0
                    while (col < columns) {
                        drawSettingBlock(
                            nvg,
                            contentX + col * (columnWidth + gap),
                            contentY + row * (itemHeight + gap),
                            columnWidth,
                            itemHeight,
                            headerHeight,
                            blockOuter,
                            blockInner,
                            accentColor,
                            linePrimary,
                            lineSecondary,
                        )
                        col++
                    }
                    row++
                }
            }

            UILayoutType.SETTINGS_ADAPTIVE -> {
                val doubleCardHeight = max(18f, (contentHeight - gap * 2f) / 3f)
                val columnWidth = max(40f, (contentWidth - gap) / 2f)
                val firstRowY = contentY
                val secondRowY = firstRowY + doubleCardHeight + gap
                val thirdRowY = secondRowY + doubleCardHeight + gap
                val bottomHeight = max(18f, contentHeight - (doubleCardHeight * 2f) - (gap * 2f))

                drawSettingBlock(
                    nvg,
                    contentX,
                    firstRowY,
                    columnWidth,
                    doubleCardHeight,
                    headerHeight,
                    blockOuter,
                    blockInner,
                    accentColor,
                    linePrimary,
                    lineSecondary,
                )
                drawSettingBlock(
                    nvg,
                    contentX + columnWidth + gap,
                    firstRowY,
                    columnWidth,
                    doubleCardHeight,
                    headerHeight,
                    blockOuter,
                    blockInner,
                    accentColor,
                    linePrimary,
                    lineSecondary,
                )

                drawSettingBlock(
                    nvg,
                    contentX,
                    secondRowY,
                    columnWidth,
                    doubleCardHeight,
                    headerHeight,
                    blockOuter,
                    blockInner,
                    accentColor,
                    linePrimary,
                    lineSecondary,
                )
                drawSettingBlock(
                    nvg,
                    contentX + columnWidth + gap,
                    secondRowY,
                    columnWidth,
                    doubleCardHeight,
                    headerHeight,
                    blockOuter,
                    blockInner,
                    accentColor,
                    linePrimary,
                    lineSecondary,
                )

                drawSettingBlock(
                    nvg,
                    contentX,
                    thirdRowY,
                    contentWidth,
                    bottomHeight,
                    headerHeight,
                    blockOuter,
                    blockInner,
                    accentColor,
                    linePrimary,
                    lineSecondary,
                )
            }

            else -> {
            }
        }
    }

    /**
     * Draws a single settings item preview block.
     */
    private fun drawSettingBlock(
        nvg: NanoVGManager,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        headerHeight: Float,
        outer: Color,
        inner: Color,
        accentColor: Color,
        primaryLine: Color,
        secondaryLine: Color,
    ) {
        val cardY = y + headerHeight + 2f
        val cardHeight = max(8f, height - headerHeight - 2f)

        nvg.drawRoundedRect(x, y, 7f, headerHeight, 1.6f, accentColor)
        nvg.drawRoundedRect(x + 9f, y, max(8f, width - 9f), headerHeight, 1.6f, primaryLine)

        nvg.drawRoundedRect(x, cardY, width, cardHeight, 5.5f, outer)
        nvg.drawRoundedRect(x + 1f, cardY + 1f, width - 2f, cardHeight - 2f, 5f, inner)

        val lineY = cardY + 4.8f
        nvg.drawRoundedRect(x + 6f, lineY, max(10f, width - 18f), 2.4f, 1.2f, primaryLine)
        nvg.drawRoundedRect(x + 6f, lineY + 4.5f, max(9f, width - 26f), 2.2f, 1.1f, secondaryLine)
    }
}
