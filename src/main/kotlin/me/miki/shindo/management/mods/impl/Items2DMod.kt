package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.notification.NotificationType

class Items2DMod :
    Mod(TranslateText.ITEMS_2D, TranslateText.ITEMS_2D_DESCRIPTION, ModCategory.RENDER, LegacyIcon.MOD_ITEMS2_D) {
    init {
        instance = this
    }

    public override fun onEnable() {
        super.onEnable()

        val itemPhysics = ItemPhysicsMod.instance
        if (itemPhysics != null && itemPhysics.isToggled()) {
            itemPhysics.setToggled(false)
            getInstance().notificationManager.post(
                TranslateText.ITEM_PHYSICS.getText(),
                "Disabled due to incompatibility",
                NotificationType.WARNING
            )
        }
    }

    companion object {
        @JvmField
        var instance: Items2DMod? = null
    }
}




