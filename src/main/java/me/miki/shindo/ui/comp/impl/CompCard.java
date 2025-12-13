package me.miki.shindo.ui.comp.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.ui.scene.theme.SceneTheme;
import me.miki.shindo.utils.ColorUtils;

import java.awt.Color;

/**
 * Standardized card surface with optional header. Keeps styling consistent with the NanoVG container helper.
 */
public class CompCard extends Comp {

    @Setter
    private String title;
    @Setter
    private String subtitle;
    @Setter
    private String icon;
    @Setter
    private float radius = 10F;
    @Getter
    private float headerHeight;

    public CompCard() {
        super(0, 0);
        setWidth(200F);
        setHeight(120F);
    }

    public float contentX() {
        return getX() + 12F;
    }

    public float contentY() {
        return getY() + headerHeight + 10F;
    }

    public float contentWidth() {
        return getWidth() - 24F;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        draw(UIContext.get(), mouseX, mouseY, partialTicks, null);
    }

    public void draw(UIContext ctx, int mouseX, int mouseY, float partialTicks, Runnable content) {
        if (!isVisible()) {
            return;
        }

        SceneTheme theme = ctx.theme();
        ColorPalette palette = ctx.palette();
        AccentColor accent = ctx.accent();

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        ctx.nvg().drawContainer(x, y, width, height, radius, palette);

        boolean hasHeader = title != null || subtitle != null || icon != null;
        headerHeight = hasHeader ? 32F : 0F;

        if (hasHeader) {
            Color headerColor = ColorUtils.applyAlpha(theme.secondaryOr(palette.getBackgroundColor(ColorType.MID)), 200);
            ctx.nvg().drawRoundedRect(x + 1F, y + 1F, width - 2F, headerHeight, Math.max(0F, radius - 1F), headerColor);

            float textX = x + 12F;
            float textY = y + 10F;

            if (icon != null) {
                ctx.nvg().drawText(icon, textX, textY, theme.getTextPrimary(), 12F, Fonts.ICON_OUTLINE);
                textX += ctx.nvg().getTextWidth(icon, 12F, Fonts.ICON_OUTLINE) + 6F;
            }

            if (title != null) {
                ctx.nvg().drawText(title, textX, textY, theme.getTextPrimary(), 12F, Fonts.SEMIBOLD);
            }
            if (subtitle != null) {
                ctx.nvg().drawText(subtitle, textX, textY + 12F, theme.getTextSecondary(), 10F, Fonts.REGULAR);
            }
        } else {
            headerHeight = 0F;
        }

        float accentAlpha = 60F;
        Color accentStroke = ColorUtils.applyAlpha(accent.getColor2(), (int) accentAlpha);
        ctx.nvg().drawOutlineRoundedRect(x, y, width, height, radius, 1.2F, accentStroke);

        if (content != null) {
            ctx.nvg().save();
            ctx.nvg().translate(contentX(), contentY());
            content.run();
            ctx.nvg().restore();
        }
    }
}
