package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;

public class AnimationsMod extends Mod {

    private static AnimationsMod instance;

    private final BooleanSetting blockHitSetting = new BooleanSetting(TranslateText.BLOCK_HIT, this, true);
    private final BooleanSetting pushingSetting = new BooleanSetting(TranslateText.PUSHING, this, true);
    private final BooleanSetting pushingParticleSetting = new BooleanSetting(TranslateText.PUSHING_PARTICLES, this, true);
    private final BooleanSetting sneakSetting = new BooleanSetting(TranslateText.SNEAK, this, true);
    private final BooleanSetting smoothSneakSetting = new BooleanSetting(TranslateText.SNEAKSMOOTH, this, false);
    private final NumberSetting smoothSneakSpeedSetting = new NumberSetting(TranslateText.SMOOTH_SPEED, this, 6, 0.5, 20, false);
    private final BooleanSetting healthSetting = new BooleanSetting(TranslateText.HEALTH, this, true);

    private final BooleanSetting armorDamageSetting = new BooleanSetting(TranslateText.ARMOR_DAMAGE, this, false);
    private final BooleanSetting itemSwitchSetting = new BooleanSetting(TranslateText.ITEM_SWITCH, this, false);
    private final BooleanSetting rodSetting = new BooleanSetting(TranslateText.ROD, this, false);

    public AnimationsMod() {
        super(TranslateText.OLD_ANIMATION, TranslateText.OLD_ANIMATION_DESCRIPTION, ModCategory.RENDER, "oldoam1.7smoothsneak");

        instance = this;
    }

    public static AnimationsMod getInstance() {
        return instance;
    }

    public BooleanSetting getBlockHitSetting() {
        return blockHitSetting;
    }

    public BooleanSetting getPushingSetting() {
        return pushingSetting;
    }

    public BooleanSetting getPushingParticleSetting() {
        return pushingParticleSetting;
    }

    public BooleanSetting getSneakSetting() {
        return sneakSetting;
    }

    public BooleanSetting getSmoothSneakSetting() {
        return smoothSneakSetting;
    }

    public float getSmoothSneakSpeedSetting() {
        return smoothSneakSpeedSetting.getValueFloat();
    }

    public BooleanSetting getHealthSetting() {
        return healthSetting;
    }

    public BooleanSetting getArmorDamageSetting() {
        return armorDamageSetting;
    }

    public BooleanSetting getItemSwitchSetting() {
        return itemSwitchSetting;
    }

    public BooleanSetting getRodSetting() {
        return rodSetting;
    }
}
