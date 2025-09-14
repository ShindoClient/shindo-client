package me.miki.shindo.management.addons.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.settings.AddonSetting;
import me.miki.shindo.management.language.TranslateText;

public class NumberSetting extends AddonSetting {

    @Getter
    private final double defaultValue;
    @Getter
    private final double minValue;
    @Getter
    private final double maxValue;
    private final boolean integer;

    @Setter
    private double value;

    public NumberSetting(String text, Addon parent, double defaultValue, double minValue, double maxValue, boolean integer) {
        super(text, parent);

        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.integer = integer;

        Shindo.getInstance().getAddonManager().addSettings(this);
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

}
