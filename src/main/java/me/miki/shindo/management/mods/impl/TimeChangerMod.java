package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;

public class TimeChangerMod extends Mod {

    @Getter
    private static TimeChangerMod instance;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.TIME, min = 0, max = 24, current = 12)
    private double timeSetting = 12;

    public TimeChangerMod() {
        super(TranslateText.TIME_CHANGER, TranslateText.TIME_CHANGER_DESCRIPTION, ModCategory.WORLD);

        instance = this;
    }

    public NumberSetting getTimeSetting() {
        return SettingRegistry.getNumberSetting(this, "timeSetting");
    }

}
