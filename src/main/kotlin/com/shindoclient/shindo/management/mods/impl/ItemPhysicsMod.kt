package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.settings.impl.NumberSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getNumberSetting

class ItemPhysicsMod :
    Mod(
        TranslateText.ITEM_PHYSICS,
        TranslateText.ITEM_PHYSICS_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_ITEM_PHYSICS,
    ) {
    @Property(type = PropertyType.NUMBER, translate = TranslateText.SPEED, min = 0.5, max = 4.0, current = 1.0)
    @JvmField
    var speedSetting = 1.0

    init {
        instance = this
    }

    fun getSpeedSetting(): NumberSetting? = getNumberSetting(this, "speedSetting")

    override fun onEnable() {
        super.onEnable()

        val items2D = Items2DMod.instance
        if (items2D != null && items2D.isToggled()) {
            items2D.setToggled(false)
        }
    }

    companion object {
        @JvmField
        var instance: ItemPhysicsMod? = null
    }
}
