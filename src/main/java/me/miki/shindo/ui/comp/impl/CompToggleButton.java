package me.miki.shindo.ui.comp.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;

import java.awt.*;

public class CompToggleButton extends Comp {

    private final SimpleAnimation opacityAnimation = new SimpleAnimation();
    private final SimpleAnimation toggleAnimation = new SimpleAnimation();
    private final ColorAnimation circleAnimation = new ColorAnimation();

    private final BooleanSetting setting;

    private float scale;

    public CompToggleButton(float x, float y, float scale, BooleanSetting setting) {
        super(x, y);

        ColorPalette palette = Shindo.getInstance().getColorManager().getPalette();

        this.setting = setting;
        this.scale = scale;
        toggleAnimation.setValue(setting.isToggled() ? 20.5F : 2.5F);
        circleAnimation.setColor(setting.isToggled() ? Color.WHITE : palette.getBackgroundColor(ColorType.DARK));
    }

    public CompToggleButton(BooleanSetting setting) {
        super(0, 0);

        ColorPalette palette = Shindo.getInstance().getColorManager().getPalette();

        this.setting = setting;
        this.scale = 1.0F;
        toggleAnimation.setValue(setting.isToggled() ? 20.5F : 2.5F);
        circleAnimation.setColor(setting.isToggled() ? Color.WHITE : palette.getBackgroundColor(ColorType.DARK));
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        AccentColor accentColor = colorManager.getCurrentColor();
        ColorPalette palette = colorManager.getPalette();

        float x = this.getX();
        float y = this.getY();
        float width = 34 * scale;
        float height = 16 * scale;
        float circle = 11 * scale;
        boolean toggled = setting.isToggled();

        opacityAnimation.setAnimation(toggled ? 1.0F : 0.0F, 14);
        toggleAnimation.setAnimation(toggled ? 20.5F : 2.5F, 14);

        nvg.drawRoundedRect(x, y, width, height, (7 * scale), palette.getBackgroundColor(ColorType.NORMAL));
        nvg.drawGradientRoundedRect(x, y, width, height, (7.4F * scale), ColorUtils.applyAlpha(accentColor.getColor1(), (int) (opacityAnimation.getValue() * 255)), ColorUtils.applyAlpha(accentColor.getColor2(), (int) (opacityAnimation.getValue() * 255)));
        nvg.drawRoundedRect(x + (toggleAnimation.getValue() * scale), y + (2.5F * scale), circle, circle, circle / 2, circleAnimation.getColor(toggled ? Color.WHITE : palette.getBackgroundColor(ColorType.DARK), 16));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        float x = this.getX();
        float y = this.getY();
        float width = 34 * scale;
        float height = 16 * scale;

        if (MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && mouseButton == 0) {
            setting.setToggled(!setting.isToggled());
        }
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public BooleanSetting getSetting() {
        return setting;
    }
}
