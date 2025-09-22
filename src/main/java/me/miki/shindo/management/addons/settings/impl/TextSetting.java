package me.miki.shindo.management.addons.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.settings.AddonSetting;

@Getter
public class TextSetting extends AddonSetting {

    private final String defaultText;
    @Setter
    private String text;

    public TextSetting(String tText, Addon parent, String text) {
        super(tText, parent);
        this.text = text;
        this.defaultText = text;

        Shindo.getInstance().getAddonManager().addSettings(this);
    }

    @Override
    public void reset() {
        this.text = defaultText;
    }

}
