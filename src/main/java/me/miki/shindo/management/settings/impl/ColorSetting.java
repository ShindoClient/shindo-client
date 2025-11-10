package me.miki.shindo.management.settings.impl;

import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.utils.ColorUtils;

import java.awt.*;

public class ColorSetting extends Setting {

    private final Color defaultColor;
    private final boolean showAlpha;
    private float hue, saturation, brightness;
    private int alpha;
    private Color color;

    public ColorSetting(TranslateText text, ConfigOwner parent, Color color, boolean showAlpha) {
        super(text, parent);

        this.color = color;
        this.defaultColor = color;
        this.hue = ColorUtils.getHue(color);
        this.saturation = ColorUtils.getSaturation(color);
        this.brightness = ColorUtils.getBrightness(color);
        this.alpha = color.getAlpha();
        this.showAlpha = showAlpha;
    }

    public ColorSetting(String name, ConfigOwner parent, Color color, boolean showAlpha) {
        super(name, parent);

        this.color = color;
        this.defaultColor = color;
        this.hue = ColorUtils.getHue(color);
        this.saturation = ColorUtils.getSaturation(color);
        this.brightness = ColorUtils.getBrightness(color);
        this.alpha = color.getAlpha();
        this.showAlpha = showAlpha;
    }

    @Override
    public void reset() {
        this.color = defaultColor;
        this.hue = ColorUtils.getHue(color);
        this.saturation = ColorUtils.getSaturation(color);
        this.brightness = ColorUtils.getBrightness(color);
        this.alpha = color.getAlpha();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getDefaultColor() {
        return defaultColor;
    }

    public float getHue() {
        return hue;
    }

    public void setHue(float hue) {
        this.hue = hue;
        this.color = ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha);
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
        this.color = ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha);
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
        this.color = ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha);
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
        this.color = ColorUtils.applyAlpha(color, alpha);
    }

    public boolean isShowAlpha() {
        return showAlpha;
    }
}
