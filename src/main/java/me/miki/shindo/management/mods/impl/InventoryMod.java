package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryMod extends Mod {

    private static InventoryMod instance;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ANIMATION)
    private boolean animationSetting;

    @Property(type = PropertyType.COMBO, translate = TranslateText.ANIMATION_TYPE)
    private AnimationType animationType = AnimationType.NORMAL;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BACKGROUND)
    private boolean backgroundSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PREVENT_POTION_SHIFT)
    private boolean preventPotionShiftSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PARTICLE)
    private boolean particleSetting;

    public InventoryMod() {
        super(TranslateText.INVENTORY, TranslateText.INVENTORY_DESCRIPTION, ModCategory.OTHER, LegacyIcon.MOD_INVENTORY);

        instance = this;
    }

    public static InventoryMod getInstance() {
        return instance;
    }

    public AnimationType getAnimationType() {
        return animationType;
    }

    public BooleanSetting getAnimationSetting() {
        return SettingRegistry.getBooleanSetting(this, "animationSetting");
    }

    public BooleanSetting getBackgroundSetting() {
        return SettingRegistry.getBooleanSetting(this, "backgroundSetting");
    }

    public BooleanSetting getPreventPotionShiftSetting() {
        return SettingRegistry.getBooleanSetting(this, "preventPotionShiftSetting");
    }

    public BooleanSetting getParticleSetting() {
        return SettingRegistry.getBooleanSetting(this, "particleSetting");
    }

    public enum AnimationType implements PropertyEnum {
        NORMAL(TranslateText.NORMAL),
        BACKIN(TranslateText.BACKIN);

        private final TranslateText translate;

        AnimationType(TranslateText translate) {
            this.translate = translate;
        }

        @NotNull
        @Override
        public TranslateText getTranslate() {
            return translate;
        }

    }
}




