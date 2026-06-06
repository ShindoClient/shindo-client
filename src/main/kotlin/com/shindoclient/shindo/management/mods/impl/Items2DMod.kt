package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.notification.NotificationType

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
