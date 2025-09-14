package me.miki.shindo.management.addons.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.settings.AddonSetting;

@Getter
public class BooleanSetting extends AddonSetting {

    private final boolean defaultValue;

    @Setter
    private boolean toggled;

    public BooleanSetting(String text, Addon parent, boolean toggled) {
        super(text, parent);

        this.toggled = toggled;
        this.defaultValue = toggled;

        Shindo.getInstance().getAddonManager().addSettings(this);
    }

    @Override
    public void reset() {
        this.toggled = defaultValue;
    }

}
