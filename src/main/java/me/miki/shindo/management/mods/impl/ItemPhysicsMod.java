package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;

public class ItemPhysicsMod extends Mod {

    private static ItemPhysicsMod instance;

    private final NumberSetting speedSetting = new NumberSetting(TranslateText.SPEED, this, 1, 0.5, 4, false);

    public ItemPhysicsMod() {
        super(TranslateText.ITEM_PHYSICS, TranslateText.ITEM_PHYSICS_DESCRIPTION, ModCategory.RENDER);

        instance = this;
    }

    public static ItemPhysicsMod getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (Items2DMod.getInstance().isToggled()) {
            Items2DMod.getInstance().setToggled(false);
        }
    }

    public NumberSetting getSpeedSetting() {
        return speedSetting;
    }
}
