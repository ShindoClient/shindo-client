package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.notification.NotificationType;

public class Items2DMod extends Mod {

    private static Items2DMod instance;

    public Items2DMod() {
        super(TranslateText.ITEMS_2D, TranslateText.ITEMS_2D_DESCRIPTION, ModCategory.RENDER);

        instance = this;
    }

    public static Items2DMod getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (ItemPhysicsMod.getInstance().isToggled()) {
            ItemPhysicsMod.getInstance().setToggled(false);
            Shindo.getInstance().getNotificationManager().post(TranslateText.ITEM_PHYSICS.getText(), "Disabled due to incompatibility", NotificationType.WARNING);
        }
    }
}
