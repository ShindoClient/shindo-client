package me.miki.shindo.management.addons.settings.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.settings.AddonSetting;

public class CategorySetting extends AddonSetting {

    public CategorySetting(String text, Addon parent) {
        super(text, parent);


        Shindo.getInstance().getAddonManager().addSettings(this);

    }
}
