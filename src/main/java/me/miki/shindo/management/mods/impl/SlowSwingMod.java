package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;

public class SlowSwingMod extends Mod {

    @Getter
    private static SlowSwingMod instance;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.DELAY, min = 2, max = 20, current = 14, step = 1)
    private int delaySetting = 14;

    public SlowSwingMod() {
        super(TranslateText.SLOW_SWING, TranslateText.SLOW_SWING_DESCRIPTION, ModCategory.PLAYER);

        instance = this;
    }

    public NumberSetting getDelaySetting() {
        return SettingRegistry.getNumberSetting(this, "delaySetting");
    }
}
