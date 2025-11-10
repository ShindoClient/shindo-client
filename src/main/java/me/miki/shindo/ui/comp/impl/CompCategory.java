package me.miki.shindo.ui.comp.impl;

import lombok.Getter;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
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
        setHeight(UIStyle.SETTING_TEXT_MARGIN + 18F);
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

        Color baseColor = palette.getBackgroundColor(ColorType.DARK);
        ctx.nvg().drawRoundedRect(x, y, width, height, UIStyle.CATEGORY_CORNER_RADIUS, baseColor);

        float pulse = Math.max(hoverAnimation.getValue(), 0.25F + (toggleAnimation.getValue() * 0.25F));
        Color highlightStart = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (pulse * 90));
        Color highlightEnd = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (pulse * 90));
        ctx.nvg().drawGradientRoundedRect(x, y, width, height, UIStyle.CATEGORY_CORNER_RADIUS, highlightStart, highlightEnd);

        ctx.nvg().drawRoundedRect(x, y, width, height, UIStyle.CATEGORY_CORNER_RADIUS, ColorUtils.applyAlpha(baseColor, 180));

        ctx.nvg().drawText(setting.getName(), x + 14, y + 11, palette.getFontColor(ColorType.DARK), 12, Fonts.MEDIUM);

        String icon = setting.isCollapsed() ? LegacyIcon.CHEVRON_RIGHT : LegacyIcon.CHEVRON_DOWN;
        ctx.nvg().drawText(icon, x + width - 20, y + 11, palette.getFontColor(ColorType.NORMAL), 11, Fonts.LEGACYICON);

        float dividerAlpha = 40 + (hoverAnimation.getValue() * 80);
        UIRenderer.drawDivider(ctx, x + 12, y + height - 3, width - 24, 2, 1, dividerAlpha);

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
