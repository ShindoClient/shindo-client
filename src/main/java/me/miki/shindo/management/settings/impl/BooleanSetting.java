package me.miki.shindo.management.settings.impl;

import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;

public class BooleanSetting extends Setting {

    private final boolean defaultValue;
    private boolean toggled;

    public BooleanSetting(TranslateText text, ConfigOwner parent, boolean toggled) {
        super(text, parent);
        this.toggled = toggled;
        this.defaultValue = toggled;
    }

    public BooleanSetting(String name, ConfigOwner parent, boolean toggled) {
        super(name, parent);
        this.toggled = toggled;
        this.defaultValue = toggled;
    }

    @Override
    public void reset() {
        this.toggled = defaultValue;
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggle) {
        this.toggled = toggle;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }
}
