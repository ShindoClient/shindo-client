package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getNumberSetting

class SlowSwingMod : Mod(TranslateText.SLOW_SWING, TranslateText.SLOW_SWING_DESCRIPTION, ModCategory.PLAYER, Shinconic.MOD_SLOW_SWING) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.DELAY,
        min = 2.0,
        max = 20.0,
        current = 14.0,
        step = 1.0,
    )
    @JvmField
    var delaySetting = 14.0

    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: SlowSwingMod? = null
    }

    fun getDelaySetting(): NumberSetting? = getNumberSetting(this, "delaySetting")
}
