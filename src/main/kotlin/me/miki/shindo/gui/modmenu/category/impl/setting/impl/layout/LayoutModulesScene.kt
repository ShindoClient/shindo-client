package me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout

import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
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
import kotlin.math.min

class LayoutModulesScene(parent: SettingsCategory) :
    LayoutAreaScene(
        parent,
        UILayoutManager.Layouts.MODULES,
        TranslateText.SETTINGS_LAYOUT_SECTION_MODULE,
        TranslateText.SETTINGS_LAYOUT_MODULE_DESCRIPTION,
        LegacyIcon.LIST
    ) {

    override val previewMaxHeight: Float = 152f

    override fun drawPreview(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val settingsMod = InternalSettingsMod.instance
        val layout = settingsMod.getModuleLayout()
        val columns = settingsMod.moduleGridColumns
        val clampedColumns = max(1, min(columns, 2))

        val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 170)
        val cardColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 220)
        val pillColor = ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 230)

        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, base)

        val rows = 3
        val padding = 8f
        val columnGap = 8f
        val rowGap = 8f
        val cardHeight = 28f
        var columnWidth = (width - (padding * 2f) - ((clampedColumns - 1) * columnGap)) / clampedColumns
        columnWidth = max(60f, columnWidth)

        for (row in 0 until rows) {
            for (column in 0 until clampedColumns) {
                val cardX = x + padding + column * (columnWidth + columnGap)
                val cardY = y + padding + row * (cardHeight + rowGap)
                nvg.drawRoundedRect(cardX, cardY, columnWidth, cardHeight, 6f, cardColor)
                nvg.drawRoundedRect(cardX + 9f, cardY + 11f, columnWidth - 34f, 6f, 3f, pillColor)
                nvg.drawRoundedRect(cardX + columnWidth - 20f, cardY + 10f, 12f, 12f, 6f, ColorUtils.applyAlpha(accent.color1, 200))
            }
        }
    }
}
