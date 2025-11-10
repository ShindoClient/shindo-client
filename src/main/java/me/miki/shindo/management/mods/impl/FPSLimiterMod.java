package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;

public class FPSLimiterMod extends Mod {

    private static FPSLimiterMod instance;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.LIMIT_MAX_FPS, category = "Gameplay")
    private boolean limitMaxFpsSetting = true;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.MAX_FPS, category = "Gameplay", min = 240, max = 1440, step = 1, current = 480)
    private int maxFpsSetting = 480;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.LIMIT_GUI_FPS, category = "Menus")
    private boolean limitGuiFps = true;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.GUI_FPS, category = "Menus", min = 1, max = 240, step = 1, current = 30)
    private int guiFpsSetting = 30;

    public FPSLimiterMod() {
        super(TranslateText.FPS_LIMITER, TranslateText.FPS_LIMITER_DESCRIPTION, ModCategory.OTHER);

        instance = this;
    }

    public static FPSLimiterMod getInstance() {
        return instance;
    }

    public NumberSetting getMaxFpsSetting() {
        return SettingRegistry.getNumberSetting(this, "maxFpsSetting");
    }

    public BooleanSetting getLimitGuiFps() {
        return SettingRegistry.getBooleanSetting(this, "limitGuiFps");
    }

    public NumberSetting getGuiFpsSetting() {
        return SettingRegistry.getNumberSetting(this, "guiFpsSetting");
    }

    public BooleanSetting getLimitMaxFpsSetting() {
        return SettingRegistry.getBooleanSetting(this, "limitMaxFpsSetting");
    }
}
