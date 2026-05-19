package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.notification.NotificationType

class Items2DMod : Mod(TranslateText.ITEMS_2D, TranslateText.ITEMS_2D_DESCRIPTION, ModCategory.RENDER, Shinconic.MOD_ITEMS2_D) {
    init {
        instance = this
    }

    override fun onEnable() {
        super.onEnable()

        val itemPhysics = ItemPhysicsMod.instance
        if (itemPhysics != null && itemPhysics.isToggled()) {
            itemPhysics.setToggled(false)
            Shindo.getInstance().getNotificationManager().post(
                TranslateText.ITEM_PHYSICS.getText(),
                "Disabled due to incompatibility",
                NotificationType.WARNING,
            )
        }
    }

    companion object {
        @JvmField
        var instance: Items2DMod? = null
    }
}
