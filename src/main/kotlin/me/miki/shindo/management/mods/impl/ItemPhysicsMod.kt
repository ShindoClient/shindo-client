package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.Mod.isToggled
import me.miki.shindo.management.mods.Mod.setToggled
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getNumberSetting

class ItemPhysicsMod : Mod(
    TranslateText.ITEM_PHYSICS,
    TranslateText.ITEM_PHYSICS_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_ITEM_PHYSICS
) {
    @Property(type = PropertyType.NUMBER, translate = TranslateText.SPEED, min = 0.5, max = 4, current = 1)
    @JvmField
    var speedSetting = 1.0

    init {
        instance = this
    }

    fun getSpeedSetting(): NumberSetting? = getNumberSetting(this, "speedSetting")

    public override fun onEnable() {
        super.onEnable()

        if (Items2DMod.Companion.getInstance().isToggled()) {
            Items2DMod.Companion.getInstance().setToggled(false)
        }
    }

    companion object {
        @JvmField
        var instance: ItemPhysicsMod? = null
    }
}




