package me.miki.shindo.management.addons.settings;

import lombok.Getter;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.language.TranslateText;

@Getter
public class AddonSetting {

    private final Addon parent;
    private final String name;

    public AddonSetting(String name, Addon parent) {
        this.name = name;
        this.parent = parent;
    }

    public void reset() {
    }


}
