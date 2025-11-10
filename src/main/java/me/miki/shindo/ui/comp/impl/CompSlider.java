package me.miki.shindo.ui.comp.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.utils.MathUtils;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;

public class CompSlider extends Comp {

    @Getter
    private final NumberSetting setting;
    private final SimpleAnimation animation = new SimpleAnimation();
    private final SimpleAnimation draggingAnimation = new SimpleAnimation();
    private boolean dragging;
    @Setter
    private boolean circle;
    @Setter
    private boolean showValue;

    public CompSlider(float x, float y, NumberSetting setting, float width) {
        super(x, y);
        this.setting = setting;
        setWidth(width);
        setHeight(4F);
        this.circle = true;
        this.showValue = true;
    }

    public CompSlider(NumberSetting setting) {
        super(0, 0);
        this.setting = setting;
        setWidth(90F);
        setHeight(4F);
        this.circle = true;
        this.showValue = true;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {

        UIContext ctx = ctx();
        NanoVGManager nvg = ctx.nvg();
        AccentColor accentColor = ctx.accent();
        ColorPalette palette = ctx.palette();

        double maxValue = setting.getMaxValue();
        double minValue = setting.getMinValue();
        double value = setting.getValue();

        float trackWidth = getWidth();
        float trackHeight = getHeight();
        double valueWidth = trackWidth * (value - minValue) / (maxValue - minValue);

        double diff = Math.min(trackWidth, Math.max(0, mouseX - (this.getX() - 1.5F)));

        if (dragging) {
            if (diff == 0) {
                setting.setValue(minValue);
            } else {
                double newValue = (diff / trackWidth) * (maxValue - minValue) + minValue;
                double step = setting.getStep();
                if (step > 0) {
                    newValue = Math.round(newValue / step) * step;
                }
                setting.setValue(MathUtils.roundToPlace(newValue, setting.isInteger() ? 0 : 2));
            }
        }

        animation.setAnimation((float) valueWidth, 16);
        draggingAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, this.getX() - 6, this.getY() - 3, trackWidth + 12, trackHeight * trackHeight) ? 1.0F : 0.0F, 16);

        nvg.drawRoundedRect(this.getX(), this.getY(), trackWidth, trackHeight, 2F, palette.getBackgroundColor(ColorType.NORMAL));
        nvg.drawGradientRoundedRect(this.getX(), this.getY(), animation.getValue(), trackHeight, 2F, accentColor.getColor1(), accentColor.getColor2());

        if (circle) {
            nvg.drawGradientRoundedRect(this.getX() + animation.getValue() - 6, this.getY() - 2, 8, 8, 4, accentColor.getColor1(), accentColor.getColor2());
        }

        if (showValue) {
            nvg.save();
            nvg.translate(0, 2 - (draggingAnimation.getValue() * 2));

            String display = setting.isInteger() ? Integer.toString((int) value) : String.format("%.2f", value);
            nvg.drawText(display, this.getX() + animation.getValue() - (nvg.getTextWidth(display, 7, Fonts.REGULAR) / 2), this.getY() - 10, palette.getFontColor(ColorType.NORMAL, (int) (draggingAnimation.getValue() * 255)), 7, Fonts.REGULAR);

            nvg.restore();
        }

        super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float trackWidth = getWidth();
        float trackHeight = getHeight();
        if (MouseUtils.isInside(mouseX, mouseY, this.getX() - 6, this.getY() - 3, trackWidth + 12, trackHeight * trackHeight) && mouseButton == 0) {
            dragging = true;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        dragging = false;
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }
}
