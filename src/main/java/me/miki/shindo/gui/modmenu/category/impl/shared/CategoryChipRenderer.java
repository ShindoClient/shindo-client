package me.miki.shindo.gui.modmenu.category.impl.shared;

import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.utils.ColorUtils;

import java.awt.Color;

/**
 * Consistent renderer used across mod menu categories for filter chips / segmented controls.
 */
public final class CategoryChipRenderer {

    private CategoryChipRenderer() {
    }

    public static final float CHIP_HEIGHT = 22F;
    public static final float CHIP_HORIZONTAL_PADDING = 12F;
    private static final float CHIP_RADIUS = 6F;

    public static float computeWidth(NanoVGManager nvg, String label, String icon) {
        float iconWidth = 0F;
        if (icon != null && !icon.isEmpty()) {
            iconWidth = nvg.getTextWidth(icon, 12F, Fonts.LEGACYICON) + 6F;
        }
        float textWidth = label == null ? 0F : nvg.getTextWidth(label, 9.5F, Fonts.MEDIUM);
        return CHIP_HORIZONTAL_PADDING * 2F + iconWidth + textWidth;
    }

    public static void drawChip(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, String label, String icon, boolean active, boolean hovered) {

        Color base = palette.getBackgroundColor(ColorType.DARK);
        Color background = ColorUtils.applyAlpha(base, hovered || active ? 235 : 205);

        if (active) {
            Color start = ColorUtils.applyAlpha(accent.getColor1(), 210);
            Color end = ColorUtils.applyAlpha(accent.getColor2(), 210);
            nvg.drawGradientRoundedRect(x, y, width, CHIP_HEIGHT, CHIP_RADIUS, start, end);
        } else {
            nvg.drawRoundedRect(x, y, width, CHIP_HEIGHT, CHIP_RADIUS, background);
        }

        float textX = x + CHIP_HORIZONTAL_PADDING;
        float textY = y + CHIP_HEIGHT / 2F - 1F;
        Color textColor = active ? Color.WHITE : palette.getFontColor(ColorType.NORMAL);

        if (icon != null && !icon.isEmpty()) {
            nvg.drawText(icon, textX, textY - 4F, textColor, 12F, Fonts.LEGACYICON);
            textX += nvg.getTextWidth(icon, 12F, Fonts.LEGACYICON) + 4F;
        }

        if (label != null && !label.isEmpty()) {
            nvg.drawText(label, textX, textY - 2F, textColor, 9.5F, Fonts.MEDIUM);
        }
    }
}
