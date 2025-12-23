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

class LayoutAddonsScene(parent: SettingsCategory) :
    LayoutAreaScene(
        parent,
        UILayoutManager.Layouts.ADDONS,
        TranslateText.SETTINGS_LAYOUT_SECTION_ADDON,
        TranslateText.SETTINGS_LAYOUT_ADDON_STANDARD_DESCRIPTION,
        LegacyIcon.PLUS_SQUARE
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
        val layout = InternalSettingsMod.getInstance().addonLayout

        val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 170)
        val cardColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 220)
        val detailColor = ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 230)

        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, base)

        if (layout == InternalSettingsMod.AddonLayout.ICON_CARDS) {
            val columns = 3
            val rows = 2
            val padding = 8f
            val columnGap = 8f
            val rowGap = 8f
            val cardSize = max(40f, (width - (padding * 2f) - ((columns - 1) * columnGap)) / columns)

            for (row in 0 until rows) {
                for (column in 0 until columns) {
                    val cardX = x + padding + column * (cardSize + columnGap)
                    val cardY = y + padding + row * (cardSize + rowGap)
                    nvg.drawRoundedRect(cardX, cardY, cardSize, cardSize, 8f, cardColor)
                    nvg.drawRoundedRect(cardX + 8f, cardY + 10f, cardSize - 16f, 6f, 3f, detailColor)
                    nvg.drawRoundedRect(cardX + 10f, cardY + 22f, cardSize - 20f, 5f, 2f, detailColor)
                    nvg.drawRoundedRect(cardX + cardSize - 22f, cardY + 8f, 14f, 14f, 6f, ColorUtils.applyAlpha(accent.color1, 200))
                }
            }
            return
        }

        val rows = 3
        val padding = 8f
        val rowGap = 8f
        val cardHeight = 30f
        val cardWidth = max(80f, width - (padding * 2f))

        for (row in 0 until rows) {
            val cardX = x + padding
            val cardY = y + padding + row * (cardHeight + rowGap)
            nvg.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, 6f, cardColor)
            nvg.drawRoundedRect(cardX + 10f, cardY + 11f, cardWidth - 58f, 6f, 3f, detailColor)
            nvg.drawRoundedRect(cardX + cardWidth - 28f, cardY + 9f, 16f, 12f, 6f, ColorUtils.applyAlpha(accent.color1, 200))
        }
    }
}
