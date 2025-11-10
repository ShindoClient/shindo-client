package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;

public class ItemPhysicsMod extends Mod {

    @Getter
    private static ItemPhysicsMod instance;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.SPEED, min = 0.5, max = 4, current = 1)
    private double speedSetting = 1;

    public ItemPhysicsMod() {
        super(TranslateText.ITEM_PHYSICS, TranslateText.ITEM_PHYSICS_DESCRIPTION, ModCategory.RENDER);

        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (Items2DMod.getInstance().isToggled()) {
            Items2DMod.getInstance().setToggled(false);
        }
    }

    public NumberSetting getSpeedSetting() {
        return SettingRegistry.getNumberSetting(this, "speedSetting");
    }
}
