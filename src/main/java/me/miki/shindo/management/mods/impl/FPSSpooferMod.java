package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;

public class FPSSpooferMod extends Mod {

    @Getter
    private static FPSSpooferMod instance;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.MULTIPLIER, min = 1, max = 30, current = 2, step = 1)
    private int multiplierSetting = 2;

    public FPSSpooferMod() {
        super(TranslateText.FPS_SPOOFER, TranslateText.FPS_SPOOFER_DESCRIPTION, ModCategory.OTHER, "fake");

        instance = this;
    }

    public NumberSetting getMultiplierSetting() {
        return SettingRegistry.getNumberSetting(this, "multiplierSetting");
    }
}
