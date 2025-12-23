package me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout

import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.layout.UILayoutManager
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils
import kotlin.math.max

class LayoutSettingsScene(parent: SettingsCategory) :
    LayoutAreaScene(
        parent,
        UILayoutManager.Layouts.SETTINGS,
        TranslateText.SETTINGS,
        TranslateText.SETTINGS_LAYOUT_DESCRIPTION,
        LegacyIcon.SETTINGS
    ) {

    override fun drawPreview(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val layoutMode = InternalSettingsMod.getInstance().settingsLayoutMode

        val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 170)
        val cardColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210)
        val detailColor = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 220)
        val accentColor = ColorUtils.applyAlpha(accent.color1, 200)

        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, base)

        val columns = if (layoutMode == SettingsPanel.LayoutMode.SINGLE_COLUMN) 1 else 2
        val rows = 2
        val padding = 8f
        val columnGap = 8f
        val rowGap = 8f
        val headerHeight = 6f
        val headerSpacing = 4f
        val cardHeight = 36f
        var columnWidth = (width - (padding * 2f) - ((columns - 1) * columnGap)) / columns
        columnWidth = max(60f, columnWidth)

        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val cardX = x + padding + column * (columnWidth + columnGap)
                val headerY = y + padding + row * (cardHeight + headerHeight + headerSpacing + rowGap)
                val cardY = headerY + headerHeight + headerSpacing

                nvg.drawRoundedRect(cardX, headerY, 8f, headerHeight, 2f, accentColor)
                nvg.drawRoundedRect(cardX + 10f, headerY, columnWidth - 10f, headerHeight, 3f, detailColor)

                nvg.drawRoundedRect(cardX, cardY, columnWidth, cardHeight, 8f, cardColor)
                nvg.drawRoundedRect(
                    cardX + 1f,
                    cardY + 1f,
                    columnWidth - 2f,
                    cardHeight - 2f,
                    7f,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210)
                )

                val contentX = cardX + 10f
                val contentY = cardY + 8f
                val contentWidth = columnWidth - 20f
                val lineHeight = 5f
                val innerGap = 7f

                for (i in 0 until 2) {
                    val lineY = contentY + i * (lineHeight + innerGap)
                    nvg.drawRoundedRect(contentX, lineY, 6f, lineHeight, 2f, accentColor)
                    nvg.drawRoundedRect(contentX + 10f, lineY, contentWidth - 16f, lineHeight, 2f, detailColor)
                }
            }
        }
    }
}
