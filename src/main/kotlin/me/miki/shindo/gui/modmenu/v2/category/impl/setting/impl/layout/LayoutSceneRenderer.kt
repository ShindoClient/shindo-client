package me.miki.shindo.gui.modmenu.v2.category.impl.setting.impl.layout

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

object LayoutSceneRenderer {
    /**
     * Draws the base panel used by each layout scene.
     */
    fun drawScenePanel(
        nvg: NanoVGManager,
        palette: ColorPalette,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        nvg.drawShadow(x, y, width, height, LayoutSceneStyle.PANEL_RADIUS, 7)
        nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            LayoutSceneStyle.PANEL_RADIUS,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 214),
        )
        nvg.drawRoundedRect(
            x + 1f,
            y + 1f,
            width - 2f,
            height - 2f,
            LayoutSceneStyle.PANEL_RADIUS - 1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 232),
        )
        nvg.drawGradientRoundedRect(
            x + 1f,
            y + 1f,
            width - 2f,
            height - 2f,
            LayoutSceneStyle.PANEL_RADIUS - 1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 34),
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 0),
        )
        nvg.drawOutlineRoundedRect(
            x,
            y,
            width,
            height,
            LayoutSceneStyle.PANEL_RADIUS,
            1f,
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 74),
        )
    }

    /**
     * Draws one selectable scene card in the layout scene index.
     */
    fun drawSceneEntryCard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        icon: String,
        title: String,
        description: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        hovered: Boolean,
        active: Boolean,
    ) {
        val highlight = hovered || active
        val baseColor =
            ColorUtils.applyAlpha(
                palette.getBackgroundColor(ColorType.MID),
                if (active) {
                    214
                } else if (highlight) {
                    198
                } else {
                    182
                },
            )
        nvg.drawShadow(x, y, width, height, 10f, if (highlight) 6 else 4)
        nvg.drawRoundedRect(x, y, width, height, 10f, baseColor)

        val borderColor =
            ColorUtils.applyAlpha(
                palette.getFontColor(ColorType.NORMAL),
                if (active) {
                    146
                } else if (highlight) {
                    120
                } else {
                    82
                },
            )
        nvg.drawOutlineRoundedRect(x, y, width, height, 10f, 1f, borderColor)

        nvg.drawGradientRoundedRect(
            x,
            y,
            width,
            height,
            10f,
            ColorUtils.applyAlpha(accent.getColor1(), if (highlight) 52 else 32),
            ColorUtils.applyAlpha(accent.getColor2(), if (highlight) 52 else 32),
        )

        val iconSize = 30f
        val iconX = x + 14f
        val iconY = y + (height - iconSize) / 2f
        nvg.drawGradientRoundedRect(
            iconX,
            iconY,
            iconSize,
            iconSize,
            9f,
            ColorUtils.applyAlpha(accent.getColor1(), 172),
            ColorUtils.applyAlpha(accent.getColor2(), 172),
        )
        nvg.drawCenteredText(
            icon,
            iconX + iconSize / 2f - 1f,
            iconY + iconSize / 2f - 8f,
            Color.WHITE,
            17f,
            Fonts.LUCIDE,
        )

        val textX = iconX + iconSize + 12f
        val textWidth = width - (textX - x) - 26f
        val clippedTitle = nvg.getLimitText(title, 11.5f, Fonts.MEDIUM, textWidth)
        val clippedDesc = nvg.getLimitText(description, 8.4f, Fonts.REGULAR, textWidth)

        nvg.drawText(clippedTitle, textX, y + 18f, palette.getFontColor(ColorType.DARK), 11.5f, Fonts.MEDIUM)
        if (!description.equals("null", ignoreCase = true)) {
            nvg.drawText(
                clippedDesc,
                textX,
                y + 33f,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 206),
                8.4f,
                Fonts.REGULAR,
            )
        }
    }

    /**
     * Draws a reusable preview surface that all scenes can paint into.
     */
    fun drawPreviewSurface(
        nvg: NanoVGManager,
        palette: ColorPalette,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float = LayoutSceneStyle.PREVIEW_RADIUS,
    ) {
        nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            radius,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 168),
        )
        nvg.drawRoundedRect(
            x + 1f,
            y + 1f,
            width - 2f,
            height - 2f,
            radius - 1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 156),
        )
        nvg.drawOutlineRoundedRect(
            x,
            y,
            width,
            height,
            radius,
            1f,
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 72),
        )
        nvg.drawGradientRoundedRect(
            x + 1f,
            y + 1f,
            width - 2f,
            height * 0.42f,
            radius - 1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 28),
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 0),
        )
    }
}
