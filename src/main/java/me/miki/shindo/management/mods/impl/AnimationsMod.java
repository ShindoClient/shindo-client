package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;

public class AnimationsMod extends Mod {

    @Getter
    private static AnimationsMod instance;

    @Getter
    private final BooleanSetting blockHitSetting = new BooleanSetting(TranslateText.BLOCK_HIT, this, true);
    @Getter
    private final BooleanSetting pushingSetting = new BooleanSetting(TranslateText.PUSHING, this, true);
    @Getter
    private final BooleanSetting pushingParticleSetting = new BooleanSetting(TranslateText.PUSHING_PARTICLES, this, true);
    @Getter
    private final BooleanSetting sneakSetting = new BooleanSetting(TranslateText.SNEAK, this, true);
    @Getter
    private final BooleanSetting smoothSneakSetting = new BooleanSetting(TranslateText.SNEAKSMOOTH, this, false);
    private final NumberSetting smoothSneakSpeedSetting = new NumberSetting(TranslateText.SMOOTH_SPEED, this, 6, 0.5, 20, false);
    @Getter
    private final BooleanSetting healthSetting = new BooleanSetting(TranslateText.HEALTH, this, true);

    @Getter
    private final BooleanSetting armorDamageSetting = new BooleanSetting(TranslateText.ARMOR_DAMAGE, this, false);
    @Getter
    private final BooleanSetting itemSwitchSetting = new BooleanSetting(TranslateText.ITEM_SWITCH, this, false);
    @Getter
    private final BooleanSetting rodSetting = new BooleanSetting(TranslateText.ROD, this, false);

    public AnimationsMod() {
        super(TranslateText.OLD_ANIMATION, TranslateText.OLD_ANIMATION_DESCRIPTION, ModCategory.RENDER, "oldoam1.7smoothsneak");

        instance = this;
    }

    public float getSmoothSneakSpeedSetting() {
        return smoothSneakSpeedSetting.getValueFloat();
    }

}
