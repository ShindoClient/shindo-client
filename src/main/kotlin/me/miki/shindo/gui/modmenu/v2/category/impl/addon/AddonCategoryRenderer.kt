package me.miki.shindo.gui.modmenu.v2.category.impl.addon

import me.miki.shindo.gui.modmenu.v2.render.ModMenuListCardRenderer
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

/**
 * Dedicated renderer helper for addon list cards.
 */
object AddonCategoryRenderer {

    fun drawCardShell(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        hoverProgress: Float,
        indicatorWidth: Float,
        failed: Boolean
    ) {
        ModMenuListCardRenderer.drawCardShell(
            nvg = nvg,
            palette = palette,
            x = x,
            y = y,
            width = width,
            height = height,
            hoverProgress = hoverProgress
        )
        if (indicatorWidth > 0f) {
            val loadColor = if (failed) Color(200, 60, 60, 220) else Color(60, 180, 80, 220)
            nvg.drawRoundedRect(x, y, indicatorWidth, height, 4f, loadColor)
            if (!failed) {
                nvg.drawGradientRoundedRect(
                    x,
                    y,
                    indicatorWidth,
                    height,
                    4f,
                    ColorUtils.applyAlpha(accent.getColor1(), 150),
                    ColorUtils.applyAlpha(accent.getColor2(), 150)
                )
            }
        }
    }

    fun drawFailedText(
        nvg: NanoVGManager,
        palette: ColorPalette,
        textX: Float,
        cardY: Float,
        failedName: String,
        failedDescription: String
    ) {
        nvg.drawText(failedName, textX, cardY + 14f, palette.getFontColor(ColorType.DARK), 11.5f, Fonts.MEDIUM)
        nvg.drawText(
            failedDescription,
            textX,
            cardY + 26f,
            palette.getFontColor(ColorType.NORMAL),
            8.5f,
            Fonts.REGULAR
        )
    }

    fun drawAddonIdentity(
        nvg: NanoVGManager,
        palette: ColorPalette,
        icon: String,
        iconCenterX: Float,
        iconCenterY: Float,
        name: String,
        textX: Float,
        cardY: Float,
        description: String,
        builtIn: Boolean
    ) {
        if (icon.isNotEmpty()) {
            nvg.drawCenteredText(
                icon,
                iconCenterX,
                iconCenterY,
                palette.getFontColor(ColorType.DARK),
                24f,
                Fonts.LEGACYICON
            )
        }

        nvg.drawText(name, textX, cardY + 14f, palette.getFontColor(ColorType.DARK), 11.5f, Fonts.MEDIUM)
        if (!builtIn) {
            nvg.drawCenteredText(
                LegacyIcon.EXTERNAL_LINK,
                textX + nvg.getTextWidth(name, 11.5f, Fonts.MEDIUM) + 14f,
                cardY + 15f,
                palette.getFontColor(ColorType.NORMAL),
                9f,
                Fonts.LEGACYICON
            )
        }

        nvg.drawText(description, textX, cardY + 26f, palette.getFontColor(ColorType.NORMAL), 8.5f, Fonts.REGULAR)
    }

    fun drawCardControls(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        hasSettings: Boolean,
        settingsX: Float,
        settingsY: Float,
        settingsSize: Float,
        settingsHoverProgress: Float,
        showToggle: Boolean,
        toggleX: Float,
        toggleY: Float,
        toggleWidth: Float,
        toggleHeight: Float,
        toggleProgress: Float
    ) {
        if (hasSettings) {
            ModMenuListCardRenderer.drawSettingsButton(
                nvg = nvg,
                palette = palette,
                accent = accent,
                x = settingsX,
                y = settingsY,
                size = settingsSize,
                hoverProgress = settingsHoverProgress
            )
        }

        if (showToggle) {
            ModMenuListCardRenderer.drawToggle(
                nvg = nvg,
                palette = palette,
                accent = accent,
                x = toggleX,
                y = toggleY,
                width = toggleWidth,
                height = toggleHeight,
                progress = toggleProgress
            )
        }
    }
}
