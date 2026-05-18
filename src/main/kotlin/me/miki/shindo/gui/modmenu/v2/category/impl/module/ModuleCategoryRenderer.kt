package me.miki.shindo.gui.modmenu.v2.category.impl.module

import me.miki.shindo.gui.modmenu.v2.render.ModMenuListCardRenderer
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import java.awt.Color

object ModuleCategoryRenderer {
    fun drawCard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        hoverProgress: Float,
        icon: String?,
        iconCenterX: Float,
        iconCenterY: Float,
        iconFontSize: Float,
        name: String,
        description: String,
        textX: Float,
        nameY: Float,
        descriptionY: Float,
        restricted: Boolean,
        warningY: Float,
        hasSettings: Boolean,
        settingsX: Float,
        settingsY: Float,
        settingsSize: Float,
        settingsHoverProgress: Float,
        toggleX: Float,
        toggleY: Float,
        toggleWidth: Float,
        toggleHeight: Float,
        toggleProgress: Float,
    ) {
        ModMenuListCardRenderer.drawCardShell(
            nvg = nvg,
            palette = palette,
            x = x,
            y = y,
            width = width,
            height = height,
            hoverProgress = hoverProgress,
        )

        if (!icon.isNullOrEmpty()) {
            nvg.drawCenteredText(
                icon,
                iconCenterX,
                iconCenterY,
                palette.getFontColor(ColorType.DARK),
                iconFontSize,
                Fonts.LEGACYICON,
            )
        }

        nvg.drawText(name, textX, nameY, palette.getFontColor(ColorType.DARK), 11.5f, Fonts.MEDIUM)
        nvg.drawText(description, textX, descriptionY, palette.getFontColor(ColorType.NORMAL), 8.5f, Fonts.REGULAR)

        if (restricted) {
            nvg.drawText(LegacyIcon.INFO, textX, warningY - 2f, Color(255, 180, 90), 8.5f, Fonts.LEGACYICON)
            nvg.drawText("Restricted on some servers", textX + 10f, warningY, Color(255, 180, 90), 8f, Fonts.REGULAR)
        }

        if (hasSettings) {
            ModMenuListCardRenderer.drawSettingsButton(
                nvg = nvg,
                palette = palette,
                accent = accent,
                x = settingsX,
                y = settingsY,
                size = settingsSize,
                hoverProgress = settingsHoverProgress,
            )
        }

        ModMenuListCardRenderer.drawToggle(
            nvg = nvg,
            palette = palette,
            accent = accent,
            x = toggleX,
            y = toggleY,
            width = toggleWidth,
            height = toggleHeight,
            progress = toggleProgress,
        )
    }
}
