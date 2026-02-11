package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
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




