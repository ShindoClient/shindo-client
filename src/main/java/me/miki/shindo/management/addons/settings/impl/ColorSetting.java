package me.miki.shindo.management.addons.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.settings.AddonSetting;
import me.miki.shindo.utils.ColorUtils;

import java.awt.*;

@Getter
public class ColorSetting extends AddonSetting {

    private final Color defaultColor;
    private final boolean showAlpha;
    private float hue, saturation, brightness;
    private int alpha;
    @Setter
    private Color color;

    public ColorSetting(String text, Addon parent, Color color, boolean showAlpha) {
        super(text, parent);

        this.color = color;
        this.defaultColor = color;
        this.hue = ColorUtils.getHue(color);
        this.saturation = ColorUtils.getSaturation(color);
        this.brightness = ColorUtils.getBrightness(color);
        this.alpha = color.getAlpha();
        this.showAlpha = showAlpha;

        Shindo.getInstance().getAddonManager().addSettings(this);
    }

    @Override
    public void reset() {
        this.color = defaultColor;
        this.hue = ColorUtils.getHue(color);
        this.saturation = ColorUtils.getSaturation(color);
        this.brightness = ColorUtils.getBrightness(color);
        this.alpha = color.getAlpha();
    }

    public void setHue(float hue) {
        this.hue = hue;
        this.color = ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha);
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
        this.color = ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha);
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
        this.color = ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha);
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
        this.color = ColorUtils.applyAlpha(color, alpha);
    }

}
