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
import me.miki.shindo.management.screenshot.ScreenshotDisplayMode
import me.miki.shindo.utils.ColorUtils

class LayoutScreenshotsScene(parent: SettingsCategory) :
    LayoutAreaScene(
        parent,
        UILayoutManager.Layouts.SCREENSHOTS,
        TranslateText.SCREENSHOT,
        TranslateText.SETTINGS_LAYOUT_SECTION_SCREENSHOT,
        LegacyIcon.CAMERA
    ) {

    override val previewMaxHeight: Float = 146f

    override fun drawPreview(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val mode = InternalSettingsMod.instance.screenshotDisplayMode
        val background = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 140)
        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, background)

        if (mode == ScreenshotDisplayMode.GRID) {
            drawGridPreview(nvg, palette, x, y, width, height)
        } else {
            drawFilmstripPreview(nvg, palette, accent, x, y, width, height)
        }
    }

    private fun drawGridPreview(nvg: NanoVGManager, palette: ColorPalette, x: Float, y: Float, width: Float, height: Float) {
        val columns = 3
        val rows = 2
        val padding = 16f
        val columnGap = 10f
        val rowGap = 10f
        val availableWidth = width - (padding * 2f)
        val availableHeight = height - (padding * 2f)
        val cellWidth = (availableWidth - ((columns - 1) * columnGap)) / columns
        val cellHeight = (availableHeight - ((rows - 1) * rowGap)) / rows

        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val cellX = x + padding + column * (cellWidth + columnGap)
                val cellY = y + padding + row * (cellHeight + rowGap)
                nvg.drawRoundedRect(cellX, cellY, cellWidth, cellHeight, 8f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210))
            }
        }
    }

    private fun drawFilmstripPreview(nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, x: Float, y: Float, width: Float, height: Float) {
        val padding = 16f
        val mainHeight = height - 42f
        nvg.drawRoundedRect(
            x + padding,
            y + padding,
            width - (padding * 2f),
            mainHeight,
            10f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210)
        )

        val stripY = y + height - 26f
        nvg.drawRoundedRect(
            x + padding,
            stripY,
            width - (padding * 2f),
            12f,
            6f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 220)
        )

        val thumbWidth = 18f
        val thumbGap = 6f
        val startX = x + padding + 4f
        for (i in 0 until 5) {
            val thumbX = startX + i * (thumbWidth + thumbGap)
            nvg.drawRoundedRect(thumbX, stripY + 2f, thumbWidth, 8f, 3f, ColorUtils.applyAlpha(accent.color1, if (i == 0) 180 else 90))
        }
    }
}
