package me.miki.shindo.management.mods.settings.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.settings.Setting;

public class BooleanSetting extends Setting {

    private final boolean defaultValue;
    private boolean toggled;

    public BooleanSetting(TranslateText text, Mod parent, boolean toggled) {
        super(text, parent);

        this.toggled = toggled;
        this.defaultValue = toggled;

        Shindo.getInstance().getModManager().addSettings(this);
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
