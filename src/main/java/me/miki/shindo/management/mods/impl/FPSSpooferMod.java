package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;

public class FPSSpooferMod extends Mod {

    private static FPSSpooferMod instance;

    private final NumberSetting multiplierSetting = new NumberSetting(TranslateText.MULTIPLIER, this, 2, 1, 30, true);

    public FPSSpooferMod() {
        super(TranslateText.FPS_SPOOFER, TranslateText.FPS_SPOOFER_DESCRIPTION, ModCategory.OTHER, "fake");

        instance = this;
    }

    public static FPSSpooferMod getInstance() {
        return instance;
    }

    public NumberSetting getMultiplierSetting() {
        return multiplierSetting;
    }
}
