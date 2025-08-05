package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;

public class FPSLimiterMod extends Mod {

    private static FPSLimiterMod instance;

    private final BooleanSetting limitMaxFpsSetting = new BooleanSetting(TranslateText.LIMIT_MAX_FPS, this, true);
    private final NumberSetting maxFpsSetting = new NumberSetting(TranslateText.MAX_FPS, this, 480, 240, 1440, true);
    private final BooleanSetting limitGuiFps = new BooleanSetting(TranslateText.LIMIT_GUI_FPS, this, true);
    private final NumberSetting guiFpsSetting = new NumberSetting(TranslateText.GUI_FPS, this, 30, 1, 240, true);

    public FPSLimiterMod() {
        super(TranslateText.FPS_LIMITER, TranslateText.FPS_LIMITER_DESCRIPTION, ModCategory.OTHER);

        instance = this;
    }

    public static FPSLimiterMod getInstance() {
        return instance;
    }

    public NumberSetting getMaxFpsSetting() {
        return maxFpsSetting;
    }

    public BooleanSetting getLimitGuiFps() {
        return limitGuiFps;
    }

    public NumberSetting getGuiFpsSetting() {
        return guiFpsSetting;
    }

    public BooleanSetting getLimitMaxFpsSetting() {
        return limitMaxFpsSetting;
    }
}
