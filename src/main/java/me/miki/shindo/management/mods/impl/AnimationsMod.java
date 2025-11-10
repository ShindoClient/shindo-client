package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;

public class AnimationsMod extends Mod {

    @Getter
    private static AnimationsMod instance;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BLOCK_HIT)
    private boolean blockHitSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PUSHING)
    private boolean pushingSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PUSHING_PARTICLES)
    private boolean pushingParticleSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SNEAK)
    private boolean sneakSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SNEAKSMOOTH)
    private boolean smoothSneakSetting = false;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.SMOOTH_SPEED, min = 0.5, max = 20, step = 0.5)
    private double smoothSneakSpeedSetting = 6;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HEALTH)
    private boolean healthSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ARMOR_DAMAGE)
    private boolean armorDamageSetting = false;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ITEM_SWITCH)
    private boolean itemSwitchSetting = false;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ROD)
    private boolean rodSetting = true;

    public AnimationsMod() {
        super(TranslateText.OLD_ANIMATION, TranslateText.OLD_ANIMATION_DESCRIPTION, ModCategory.RENDER, "oldoam1.7smoothsneak");

        instance = this;
    }

    public float getSmoothSneakSpeedSetting() {
        return (float) smoothSneakSpeedSetting;
    }

    public BooleanSetting getBlockHitSetting() {
        return SettingRegistry.getBooleanSetting(this, "blockHitSetting");
    }

    public BooleanSetting getPushingSetting() {
        return SettingRegistry.getBooleanSetting(this, "pushingSetting");
    }

    public BooleanSetting getPushingParticleSetting() {
        return SettingRegistry.getBooleanSetting(this, "pushingParticleSetting");
    }

    public BooleanSetting getSneakSetting() {
        return SettingRegistry.getBooleanSetting(this, "sneakSetting");
    }

    public BooleanSetting getSmoothSneakSetting() {
        return SettingRegistry.getBooleanSetting(this, "smoothSneakSetting");
    }

    public NumberSetting getSmoothSneakSpeedSettingProperty() {
        return SettingRegistry.getNumberSetting(this, "smoothSneakSpeedSetting");
    }

    public BooleanSetting getHealthSetting() {
        return SettingRegistry.getBooleanSetting(this, "healthSetting");
    }

    public BooleanSetting getArmorDamageSetting() {
        return SettingRegistry.getBooleanSetting(this, "armorDamageSetting");
    }

    public BooleanSetting getItemSwitchSetting() {
        return SettingRegistry.getBooleanSetting(this, "itemSwitchSetting");
    }

    public BooleanSetting getRodSetting() {
        return SettingRegistry.getBooleanSetting(this, "rodSetting");
    }
}
