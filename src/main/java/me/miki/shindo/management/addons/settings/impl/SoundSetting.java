package me.miki.shindo.management.addons.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.settings.AddonSetting;

import java.io.File;

@Getter
@Setter
public class SoundSetting extends AddonSetting {

    private File sound;

    public SoundSetting(String text, Addon parent) {
        super(text, parent);

        this.sound = null;

        Shindo.getInstance().getAddonManager().addSettings(this);
    }

    @Override
    public void reset() {
        this.sound = null;
    }

}
