package me.miki.shindo.management.settings.impl;

import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;

public class NumberSetting extends Setting {

    private final double defaultValue;
    private final double minValue;
    private final double maxValue;
    private final boolean integer;
    private double value;

    public NumberSetting(TranslateText text, ConfigOwner parent, double defaultValue, double minValue, double maxValue, boolean integer) {
        super(text, parent);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.integer = integer;
    }

    public NumberSetting(String name, ConfigOwner parent, double defaultValue, double minValue, double maxValue, boolean integer) {
        super(name, parent);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.integer = integer;
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

    public void setValue(double value) {
        if (value < getMinValue()) {
            value = getMinValue();
        }
        if (value > getMaxValue()) {
            value = getMaxValue();
        }
        if (integer) {
            value = Math.round(value);
        }
        this.value = value;
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
        if (getMetadata() != null && !Double.isNaN(getMetadata().getMin())) {
            return getMetadata().getMin();
        }
        return minValue;
    }

    public double getMaxValue() {
        if (getMetadata() != null && !Double.isNaN(getMetadata().getMax())) {
            return getMetadata().getMax();
        }
        return maxValue;
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    public double getStep() {
        if (getMetadata() != null && !Double.isNaN(getMetadata().getStep())) {
            return getMetadata().getStep();
        }
        return integer ? 1.0 : 0.0;
    }

    public boolean isInteger() {
        return integer;
    }
}
