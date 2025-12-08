package me.miki.shindo.ui.comp.impl;

import lombok.Getter;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.Icons;
import me.miki.shindo.management.settings.impl.CategorySetting;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.ui.framework.UIRenderer;
import me.miki.shindo.ui.framework.UIStyle;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;

import java.awt.Color;

public class CompCategory extends Comp {

    @Getter
    private final CategorySetting setting;

    private final SimpleAnimation toggleAnimation = new SimpleAnimation();
    private final SimpleAnimation hoverAnimation = new SimpleAnimation();

    public CompCategory(float width, CategorySetting setting) {
        super(0, 0);
        this.setting = setting;
        setWidth(width);
        setHeight(UIStyle.SETTING_TEXT_MARGIN + 10F);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        UIContext ctx = ctx();
        ColorPalette palette = ctx.palette();
        AccentColor accentColor = ctx.accent();

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();
        boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, height);

        toggleAnimation.setAnimation(setting.isCollapsed() ? 0.0F : 1.0F, 12);
        hoverAnimation.setAnimation(hovered ? 1.0F : 0.0F, 12);

        float accentPulse = Math.max(hoverAnimation.getValue(), 0.25F + (toggleAnimation.getValue() * 0.25F));
        Color baseOverlay = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), (int) (hoverAnimation.getValue() * 40));
        ctx.nvg().drawRoundedRect(x, y, width, height, UIStyle.CATEGORY_CORNER_RADIUS, baseOverlay);

        float iconSize = 11F;
        String icon = setting.isCollapsed() ? Icons.CHEVRON_RIGHT_16 : Icons.CHEVRON_DOWN_16;
        float iconHeight = ctx.nvg().getTextHeight(icon, iconSize, Fonts.ICON_OUTLINE);
        float iconX = x + 4F;
        float iconY = y + (height / 2F) - (iconHeight / 2F);
        Color iconColor = ColorUtils.interpolateColor(palette.getFontColor(ColorType.NORMAL),
                ColorUtils.applyAlpha(accentColor.getColor1(), 240), accentPulse * 0.35F);
        ctx.nvg().drawText(icon, iconX, iconY, iconColor, iconSize, Fonts.ICON_OUTLINE);

        float titleSize = 11F;
        float titleX = iconX + 14F;
        float titleHeight = ctx.nvg().getTextHeight(setting.getName(), titleSize, Fonts.MEDIUM);
        float titleY = y + (height / 2F) - (titleHeight / 2F);
        Color titleColor = ColorUtils.interpolateColor(palette.getFontColor(ColorType.DARK),
                ColorUtils.applyAlpha(accentColor.getColor2(), 230), accentPulse * 0.25F);
        ctx.nvg().drawText(setting.getName(), titleX, titleY, titleColor, titleSize, Fonts.MEDIUM);

        float underlineAlpha = 55 + (accentPulse * 85F);
        UIRenderer.drawDivider(ctx, x, y + height - 2F, width, 2F, 1.5F,
                Math.min(underlineAlpha, 140));
        super.draw(mouseX, mouseY, partialTicks);

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            setting.toggle();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
