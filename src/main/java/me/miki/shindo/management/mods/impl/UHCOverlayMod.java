package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;

public class UHCOverlayMod extends Mod {

    @Getter
    private static UHCOverlayMod instance;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.GOLD_INGOT_SCALE, min = 1.0F, max = 5.0F, current = 1.5F)
    private double goldIngotScaleSetting = 1.5F;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.GOLD_NUGGET_SCALE, min = 1.0F, max = 5.0F, current = 1.5F)
    private double goldNuggetScaleSetting = 1.5F;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.GOLD_ORE_SCALE, min = 1.0F, max = 5.0F, current = 1.5F)
    private double goldOreScaleSetting = 1.5F;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.GOLD_APPLE_SCALE, min = 1.0F, max = 5.0F, current = 1.5F)
    private double goldAppleScaleSetting = 1.5F;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.SKULL_SCALE, min = 1.0F, max = 5.0F, current = 1.5F)
    private double skullScaleSetting = 1.5F;

    public UHCOverlayMod() {
        super(TranslateText.UHC_OVERLAY, TranslateText.UHC_OVERLAY_DESCRIPTION, ModCategory.RENDER);

        instance = this;
    }

    public NumberSetting getGoldIngotScaleSetting() {
        return SettingRegistry.getNumberSetting(this, "goldIngotScaleSetting");
    }

    public NumberSetting getGoldNuggetScaleSetting() {
        return SettingRegistry.getNumberSetting(this, "goldNuggetScaleSetting");
    }

    public NumberSetting getGoldOreScaleSetting() {
        return SettingRegistry.getNumberSetting(this, "goldOreScaleSetting");
    }

    public NumberSetting getGoldAppleScaleSetting() {
        return SettingRegistry.getNumberSetting(this, "goldAppleScaleSetting");
    }

    public NumberSetting getSkullScaleSetting() {
        return SettingRegistry.getNumberSetting(this, "skullScaleSetting");
    }
}
