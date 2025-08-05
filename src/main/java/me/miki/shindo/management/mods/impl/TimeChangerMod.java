package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;

public class TimeChangerMod extends Mod {

    private static TimeChangerMod instance;

    private final NumberSetting timeSetting = new NumberSetting(TranslateText.TIME, this, 12, 0, 24, false);

    public TimeChangerMod() {
        super(TranslateText.TIME_CHANGER, TranslateText.TIME_CHANGER_DESCRIPTION, ModCategory.WORLD);

        instance = this;
    }

    public static TimeChangerMod getInstance() {
        return instance;
    }

    public NumberSetting getTimeSetting() {
        return timeSetting;
    }

}
