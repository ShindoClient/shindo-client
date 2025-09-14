package me.miki.shindo.management.addons.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.settings.AddonSetting;
import me.miki.shindo.management.language.TranslateText;

import java.io.File;

@Setter
@Getter
public class ImageSetting extends AddonSetting {

    private File image;

    public ImageSetting(String text, Addon parent) {
        super(text, parent);

        this.image = null;

        Shindo.getInstance().getAddonManager().addSettings(this);
    }

    @Override
    public void reset() {
        this.image = null;
    }

}
