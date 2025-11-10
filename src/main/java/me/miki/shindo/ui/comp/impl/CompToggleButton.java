package me.miki.shindo.ui.comp.impl;

import lombok.Getter;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;

import java.awt.*;

public class CompToggleButton extends Comp {

    private final SimpleAnimation opacityAnimation = new SimpleAnimation();
    private final SimpleAnimation toggleAnimation = new SimpleAnimation();
    private final ColorAnimation circleAnimation = new ColorAnimation();

    @Getter
    private final BooleanSetting setting;

    @Getter
    private float scale;

    public CompToggleButton(float x, float y, float scale, BooleanSetting setting) {
        super(x, y);

        this.setting = setting;
        setScale(scale);
        toggleAnimation.setValue(setting.isToggled() ? 20.5F : 2.5F);
        circleAnimation.setColor(setting.isToggled() ? Color.WHITE : ctx().palette().getBackgroundColor(ColorType.DARK));
    }

    public CompToggleButton(BooleanSetting setting) {
        super(0, 0);

        this.setting = setting;
        setScale(1.0F);
        toggleAnimation.setValue(setting.isToggled() ? 20.5F : 2.5F);
        circleAnimation.setColor(setting.isToggled() ? Color.WHITE : ctx().palette().getBackgroundColor(ColorType.DARK));
    }

    public void setScale(float scale) {
        this.scale = scale;
        super.setWidth(34F * scale);
        super.setHeight(16F * scale);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {

        UIContext ctx = ctx();
        NanoVGManager nvg = ctx.nvg();
        AccentColor accentColor = ctx.accent();
        ColorPalette palette = ctx.palette();

        float x = this.getX();
        float y = this.getY();
        float width = getWidth();
        float height = getHeight();
        float circle = 11 * scale;
        boolean toggled = setting.isToggled();

        opacityAnimation.setAnimation(toggled ? 1.0F : 0.0F, 14);
        toggleAnimation.setAnimation(toggled ? 20.5F : 2.5F, 14);

        nvg.drawRoundedRect(x, y, width, height, (7 * scale), palette.getBackgroundColor(ColorType.NORMAL));
        nvg.drawGradientRoundedRect(x, y, width, height, (7.4F * scale), ColorUtils.applyAlpha(accentColor.getColor1(), (int) (opacityAnimation.getValue() * 255)), ColorUtils.applyAlpha(accentColor.getColor2(), (int) (opacityAnimation.getValue() * 255)));
        nvg.drawRoundedRect(x + (toggleAnimation.getValue() * scale), y + (2.5F * scale), circle, circle, circle / 2, circleAnimation.getColor(toggled ? Color.WHITE : palette.getBackgroundColor(ColorType.DARK), 16));

        super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        float x = this.getX();
        float y = this.getY();
        float width = getWidth();
        float height = getHeight();

        if (MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && mouseButton == 0) {
            setting.setToggled(!setting.isToggled());
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
