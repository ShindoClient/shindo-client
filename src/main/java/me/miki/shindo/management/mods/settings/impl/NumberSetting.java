package me.miki.shindo.management.mods.settings.impl;

import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.settings.Setting;

public class NumberSetting extends Setting {

    private final double defaultValue;
    private final double minValue;
    private final double maxValue;
    private final boolean integer;

    @Setter
    private double value;

    public NumberSetting(TranslateText text, Mod parent, double defaultValue, double minValue, double maxValue, boolean integer) {
        super(text, parent);

        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.integer = integer;

        Shindo.getInstance().getModManager().addSettings(this);
    }

    @Override
    public void reset() {
        this.value = defaultValue;
    }

    public double getValue() {

        if (integer) {
            this.value = (int) value;
        }

        return value;
    }

    public int getValueInt() {

        if (integer) {
            this.value = (int) value;
        }

        return (int) value;
    }

    public float getValueFloat() {

        if (integer) {
            this.value = (int) value;
        }

        return (float) value;
    }

    public long getValueLong() {

        if (integer) {
            this.value = (int) value;
        }

        return (long) value;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getDefaultValue() {
        return defaultValue;
    }
}
