package me.miki.shindo.ui.comp.style

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.utils.ColorUtils
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

object VisualPresetModificationManager {

    enum class ModificationTarget {
        SURFACE_BACKGROUND,
        SURFACE_BORDER,
        CONTROL_BASE
    }

    private data class ModificationRule(
        val preset: InternalSettingsMod.VisualPreset,
        val target: ModificationTarget,
        val surfaceVariant: CompSurfaceVariant? = null,
        val controlVariant: CompControlVariant? = null,
        val alphaDelta: Int = 0,
        val lighten: Float = 0f,
        val darken: Float = 0f,
        val accentMix: Float = 0f,
        val accentIndex: Int = 1
    )

    private val rules = listOf(
        // LIGHT: roughly +15% luminance shift across most surfaces.
        ModificationRule(
            InternalSettingsMod.VisualPreset.LIGHT,
            ModificationTarget.SURFACE_BACKGROUND,
            CompSurfaceVariant.CANVAS,
            alphaDelta = -8,
            lighten = 0.15f
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.LIGHT,
            ModificationTarget.SURFACE_BACKGROUND,
            CompSurfaceVariant.PANEL,
            alphaDelta = -6,
            lighten = 0.15f
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.LIGHT,
            ModificationTarget.SURFACE_BACKGROUND,
            CompSurfaceVariant.CARD,
            alphaDelta = -3,
            lighten = 0.13f,
            accentMix = 0.08f,
            accentIndex = 1
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.LIGHT,
            ModificationTarget.SURFACE_BORDER,
            CompSurfaceVariant.CARD,
            alphaDelta = 14,
            accentMix = 0.18f,
            accentIndex = 1
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.LIGHT,
            ModificationTarget.CONTROL_BASE,
            controlVariant = CompControlVariant.PRIMARY,
            alphaDelta = -4,
            lighten = 0.12f,
            accentMix = 0.1f
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.LIGHT,
            ModificationTarget.CONTROL_BASE,
            controlVariant = CompControlVariant.SECONDARY,
            alphaDelta = -6,
            lighten = 0.1f
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.LIGHT,
            ModificationTarget.CONTROL_BASE,
            controlVariant = CompControlVariant.GHOST,
            alphaDelta = -10,
            lighten = 0.09f
        ),

        // DARK: roughly -15% luminance shift with deeper contrast.
        ModificationRule(
            InternalSettingsMod.VisualPreset.DARK,
            ModificationTarget.SURFACE_BACKGROUND,
            CompSurfaceVariant.CANVAS,
            alphaDelta = 10,
            darken = 0.15f
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.DARK,
            ModificationTarget.SURFACE_BACKGROUND,
            CompSurfaceVariant.PANEL,
            alphaDelta = 8,
            darken = 0.15f
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.DARK,
            ModificationTarget.SURFACE_BACKGROUND,
            CompSurfaceVariant.CARD,
            alphaDelta = 4,
            darken = 0.13f,
            accentMix = 0.08f,
            accentIndex = 2
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.DARK,
            ModificationTarget.SURFACE_BORDER,
            CompSurfaceVariant.CARD,
            alphaDelta = -6,
            accentMix = 0.2f,
            accentIndex = 2
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.DARK,
            ModificationTarget.CONTROL_BASE,
            controlVariant = CompControlVariant.PRIMARY,
            alphaDelta = 8,
            darken = 0.12f,
            accentMix = 0.08f,
            accentIndex = 2
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.DARK,
            ModificationTarget.CONTROL_BASE,
            controlVariant = CompControlVariant.SECONDARY,
            alphaDelta = 6,
            darken = 0.1f
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.DARK,
            ModificationTarget.CONTROL_BASE,
            controlVariant = CompControlVariant.GHOST,
            alphaDelta = 4,
            darken = 0.08f
        ),

        // MODERN: preserve base tone but increase separation/highlights/shadows.
        ModificationRule(
            InternalSettingsMod.VisualPreset.MODERN,
            ModificationTarget.SURFACE_BACKGROUND,
            CompSurfaceVariant.CANVAS,
            darken = 0.03f,
            accentMix = 0.07f,
            accentIndex = 1
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.MODERN,
            ModificationTarget.SURFACE_BACKGROUND,
            CompSurfaceVariant.PANEL,
            alphaDelta = 8,
            darken = 0.02f,
            accentMix = 0.12f,
            accentIndex = 1
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.MODERN,
            ModificationTarget.SURFACE_BACKGROUND,
            CompSurfaceVariant.CARD,
            alphaDelta = 10,
            lighten = 0.03f,
            accentMix = 0.14f,
            accentIndex = 2
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.MODERN,
            ModificationTarget.SURFACE_BORDER,
            CompSurfaceVariant.CARD,
            alphaDelta = 20,
            accentMix = 0.26f,
            accentIndex = 2
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.MODERN,
            ModificationTarget.CONTROL_BASE,
            controlVariant = CompControlVariant.PRIMARY,
            alphaDelta = 14,
            accentMix = 0.24f,
            accentIndex = 1
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.MODERN,
            ModificationTarget.CONTROL_BASE,
            controlVariant = CompControlVariant.SECONDARY,
            alphaDelta = 8,
            darken = 0.02f,
            accentMix = 0.14f,
            accentIndex = 2
        ),
        ModificationRule(
            InternalSettingsMod.VisualPreset.MODERN,
            ModificationTarget.CONTROL_BASE,
            controlVariant = CompControlVariant.GHOST,
            alphaDelta = 6,
            accentMix = 0.1f,
            accentIndex = 1
        )
    )

    fun applySurfaceBackground(
        preset: InternalSettingsMod.VisualPreset,
        variant: CompSurfaceVariant,
        baseColor: Color,
        palette: ColorPalette,
        accent: AccentColor
    ): Color {
        return applyRules(
            preset,
            ModificationTarget.SURFACE_BACKGROUND,
            baseColor,
            palette,
            accent
        ) { it.surfaceVariant == variant }
    }

    fun applySurfaceBorder(
        preset: InternalSettingsMod.VisualPreset,
        variant: CompSurfaceVariant,
        baseColor: Color,
        palette: ColorPalette,
        accent: AccentColor
    ): Color {
        return applyRules(
            preset,
            ModificationTarget.SURFACE_BORDER,
            baseColor,
            palette,
            accent
        ) { it.surfaceVariant == variant }
    }

    fun applyControlBase(
        preset: InternalSettingsMod.VisualPreset,
        variant: CompControlVariant,
        baseColor: Color,
        palette: ColorPalette,
        accent: AccentColor
    ): Color {
        return applyRules(
            preset,
            ModificationTarget.CONTROL_BASE,
            baseColor,
            palette,
            accent
        ) { it.controlVariant == variant }
    }

    private fun applyRules(
        preset: InternalSettingsMod.VisualPreset,
        target: ModificationTarget,
        baseColor: Color,
        palette: ColorPalette,
        accent: AccentColor,
        matchVariant: (ModificationRule) -> Boolean
    ): Color {
        val compatiblePreset = if (preset == InternalSettingsMod.VisualPreset.CLASSIC) {
            InternalSettingsMod.VisualPreset.MODERN
        } else {
            preset
        }

        val themeBrightness = calculateThemeBrightness(palette)
        var color = baseColor

        for (rule in rules) {
            if (rule.preset != compatiblePreset || rule.target != target || !matchVariant(rule)) {
                continue
            }

            val adaptiveLighten = if (compatiblePreset == InternalSettingsMod.VisualPreset.MODERN) {
                rule.lighten * (0.65f + themeBrightness * 0.6f)
            } else {
                rule.lighten
            }
            val adaptiveDarken = if (compatiblePreset == InternalSettingsMod.VisualPreset.MODERN) {
                rule.darken * (1.15f - themeBrightness * 0.55f)
            } else {
                rule.darken
            }
            val adaptiveAccentMix = if (compatiblePreset == InternalSettingsMod.VisualPreset.MODERN) {
                rule.accentMix * (0.8f + themeBrightness * 0.4f)
            } else {
                rule.accentMix
            }

            color = if (adaptiveLighten > 0f) ColorUtils.lighten(color, adaptiveLighten) else color
            color = if (adaptiveDarken > 0f) ColorUtils.darken(color, adaptiveDarken) else color

            if (adaptiveAccentMix > 0f) {
                val accentColor = if (rule.accentIndex == 2) accent.getColor2() else accent.getColor1()
                color = ColorUtils.interpolateColor(color, accentColor, adaptiveAccentMix.toDouble())
            }

            val targetAlpha = (color.alpha + rule.alphaDelta).coerceIn(0, 255)
            color = ColorUtils.applyAlpha(color, targetAlpha)
        }

        return color
    }

    private fun calculateThemeBrightness(palette: ColorPalette): Float {
        val color = palette.getBackgroundColor(ColorType.NORMAL)
        val normalized = (0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue) / 255f
        return min(1f, max(0f, normalized))
    }
}
