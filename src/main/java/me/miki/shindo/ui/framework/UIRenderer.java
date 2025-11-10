package me.miki.shindo.ui.framework;

import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.utils.ColorUtils;

import java.awt.Color;

/**
 * Helper methods that encapsulate common NanoVG drawing routines so components stay focused on layout logic.
 */
public final class UIRenderer {

    private UIRenderer() {
    }

    public static void drawSettingSurface(UIContext ctx, ColorPalette palette, AccentColor accent, float x, float y, float width, float height, float radius, float hoverProgress) {

        int overlayAlpha = (int) (18 + (hoverProgress * 26));
        int fillAlpha = (int) (120 + (hoverProgress * 32));
        int outlineAlpha = (int) (hoverProgress * 120);

        ctx.nvg().drawRoundedRect(x, y, width, height, radius, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), fillAlpha));
        ctx.nvg().drawGradientRoundedRect(x, y, width, height, radius, ColorUtils.applyAlpha(accent.getColor1(), overlayAlpha), ColorUtils.applyAlpha(accent.getColor2(), overlayAlpha));

        if (outlineAlpha > 0) {
            ctx.nvg().drawOutlineRoundedRect(x, y, width, height, radius, 1.0F, ColorUtils.applyAlpha(accent.getColor2(), outlineAlpha));
        }
    }

    public static void drawDivider(UIContext ctx, float x, float y, float width, float height, float radius, float alpha) {
        ctx.nvg().drawRoundedRect(x, y, width, height, radius, new Color(255, 255, 255, (int) alpha));
    }
}
