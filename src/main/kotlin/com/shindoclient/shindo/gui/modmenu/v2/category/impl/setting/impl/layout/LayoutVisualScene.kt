package com.shindoclient.shindo.gui.modmenu.v2.category.impl.setting.impl.layout

import com.shindoclient.shindo.gui.modmenu.v2.category.impl.SettingsCategory
import com.shindoclient.shindo.management.color.AccentColor
import com.shindoclient.shindo.management.color.palette.ColorPalette
import com.shindoclient.shindo.management.color.palette.ColorType
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.nanovg.NanoVGManager
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.ui.layout.enums.UILayoutArea
import com.shindoclient.shindo.ui.layout.enums.UILayoutType
import com.shindoclient.shindo.utils.ColorUtils
import java.awt.Color
import kotlin.math.max

/**
 * Visual presets scene.
 *
 * It previews how each preset affects global UI surfaces and emphasis.
 */
class LayoutVisualScene(
    parent: SettingsCategory,
) : LayoutCarouselScene(
        parent,
        UILayoutArea.VISUAL,
        TranslateText.PRESETS,
        TranslateText.APPEARANCE_DESCRIPTION,
        Lucide.PALETTE,
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
        val style = resolveStyle(type, palette, accent)

        nvg.drawRoundedRect(x, y, width, height, LayoutSceneStyle.PREVIEW_RADIUS, style.background)

        val pad = 10f
        val sectionGap = 8f
        val sectionHeight = max(22f, (height - pad * 2f - sectionGap * 2f) / 3f)

        var i = 0
        while (i < 3) {
            val sectionY = y + pad + i * (sectionHeight + sectionGap)
            drawVisualSection(nvg, style, x + pad, sectionY, width - pad * 2f, sectionHeight, i)
            i++
        }
    }

    /**
     * Draws one UI section sample for visual presets.
     */
    private fun drawVisualSection(
        nvg: NanoVGManager,
        style: VisualStyle,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        index: Int,
    ) {
        nvg.drawRoundedRect(x, y, width, height, 7f, style.surface)

        val accentWidth = max(18f, width * style.accentWidthFactor)
        nvg.drawGradientRoundedRect(
            x + 7f,
            y + 6f,
            accentWidth,
            max(7f, height - 12f),
            5f,
            style.accentA,
            style.accentB,
        )

        val textX = x + accentWidth + 15f
        val textWidth = max(10f, width - accentWidth - 24f)

        val primaryW =
            when (index) {
                0 -> textWidth * 0.78f
                1 -> textWidth * 0.64f
                else -> textWidth * 0.7f
            }
        val secondaryW =
            when (index) {
                0 -> textWidth * 0.58f
                1 -> textWidth * 0.5f
                else -> textWidth * 0.54f
            }

        nvg.drawRoundedRect(textX, y + 7f, primaryW, 4.6f, 2f, style.textPrimary)
        nvg.drawRoundedRect(textX, y + 14f, secondaryW, 4f, 2f, style.textSecondary)
    }

    /**
     * Returns visual palette values for each preset type.
     */
    private fun resolveStyle(
        type: UILayoutType,
        palette: ColorPalette,
        accent: AccentColor,
    ): VisualStyle =
        when (type) {
            UILayoutType.VISUAL_LIGHT -> {
                VisualStyle(
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 238),
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 248),
                    ColorUtils.applyAlpha(accent.getColor1(), 176),
                    ColorUtils.applyAlpha(accent.getColor2(), 152),
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 220),
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 192),
                    0.2f,
                )
            }

            UILayoutType.VISUAL_DARK -> {
                VisualStyle(
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 244),
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 252),
                    ColorUtils.applyAlpha(accent.getColor2(), 198),
                    ColorUtils.applyAlpha(accent.getColor1(), 174),
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 230),
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 198),
                    0.25f,
                )
            }

            else -> {
                VisualStyle(
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 236),
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 246),
                    ColorUtils.applyAlpha(accent.getColor1(), 210),
                    ColorUtils.applyAlpha(accent.getColor2(), 186),
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 232),
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 204),
                    0.34f,
                )
            }
        }

    private data class VisualStyle(
        val background: Color,
        val surface: Color,
        val accentA: Color,
        val accentB: Color,
        val textPrimary: Color,
        val textSecondary: Color,
        val accentWidthFactor: Float,
    )
}
