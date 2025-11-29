package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;

public class InventoryMod extends Mod {

    @Getter
    private static InventoryMod instance;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ANIMATION)
    private boolean animationSetting;

    @Getter
    @Property(type = PropertyType.COMBO, translate = TranslateText.ANIMATION_TYPE)
    private AnimationType animationType = AnimationType.NORMAL;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BACKGROUND)
    private boolean backgroundSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PREVENT_POTION_SHIFT)
    private boolean preventPotionShiftSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PARTICLE)
    private boolean particleSetting;

    public InventoryMod() {
        super(TranslateText.INVENTORY, TranslateText.INVENTORY_DESCRIPTION, ModCategory.OTHER);

        instance = this;
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

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}
