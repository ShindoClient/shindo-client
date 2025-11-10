package me.miki.shindo.ui.comp.impl;

import lombok.Getter;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.settings.impl.ComboSetting;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.utils.MathUtils;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;

import java.awt.*;

public class CompComboBox extends Comp {

    private final SimpleAnimation changeAnimation = new SimpleAnimation();

    @Getter
    private final ComboSetting setting;

    @Getter
    private final float width;

    private int changeDirection;

    public CompComboBox(float x, float y, float width, ComboSetting setting) {
        super(x, y);
        this.width = width;
        this.setting = setting;
        this.changeDirection = 1;
        this.changeAnimation.setValue(1);
        super.setWidth(width);
        super.setHeight(16F);
    }

    public CompComboBox(float width, ComboSetting setting) {
        super(0, 0);
        this.width = width;
        this.setting = setting;
        this.changeDirection = 1;
        this.changeAnimation.setValue(1);
        super.setWidth(width);
        super.setHeight(16F);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {

        UIContext ctx = ctx();
        NanoVGManager nvg = ctx.nvg();
        AccentColor accentColor = ctx.accent();

        changeAnimation.setAnimation(changeDirection, 16);

        nvg.drawGradientRoundedRect(this.getX(), this.getY(), width, 16, 4, accentColor.getColor1(), accentColor.getColor2());

        nvg.drawCenteredText(setting.getOption().getName(), this.getX() + (width / 2) + ((changeDirection - changeAnimation.getValue()) * 22), this.getY() + 5F, new Color(255, 255, 255, (int) (MathUtils.abs(changeAnimation.getValue() * 255))), 8, Fonts.REGULAR);

        nvg.drawText("<", this.getX() + 4, this.getY() + 4F, Color.WHITE, 10, Fonts.REGULAR);
        nvg.drawText(">", this.getX() + width - 10, this.getY() + 4F, Color.WHITE, 10, Fonts.REGULAR);

        super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        int max = setting.getOptions().size();
        int modeIndex = setting.getOptions().indexOf(setting.getOption());

        if (mouseButton == 0) {

            if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), 16, 16)) {

                changeAnimation.setValue(0);

                if (modeIndex > 0) {
                    modeIndex--;
                } else {
                    modeIndex = max - 1;
                }

                changeDirection = 1;
                setting.setOption(setting.getOptions().get(modeIndex));
            }

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + width - 16, this.getY(), 16, 16)) {

                changeAnimation.setValue(0);

                if (modeIndex < max - 1) {
                    modeIndex++;
                } else {
                    modeIndex = 0;
                }

                changeDirection = -1;
                setting.setOption(setting.getOptions().get(modeIndex));
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
