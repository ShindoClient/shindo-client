package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;

public class SlowSwingMod extends Mod {

    private static SlowSwingMod instance;

    private final NumberSetting delaySetting = new NumberSetting(TranslateText.DELAY, this, 14, 2, 20, true);

    public SlowSwingMod() {
        super(TranslateText.SLOW_SWING, TranslateText.SLOW_SWING_DESCRIPTION, ModCategory.PLAYER);

        instance = this;
    }

    public static SlowSwingMod getInstance() {
        return instance;
    }

    public NumberSetting getDelaySetting() {
        return delaySetting;
    }
}
