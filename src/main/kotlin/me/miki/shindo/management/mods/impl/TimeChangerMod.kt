package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getNumberSetting

class TimeChangerMod :
    Mod(
        TranslateText.TIME_CHANGER,
        TranslateText.TIME_CHANGER_DESCRIPTION,
        ModCategory.WORLD,
        LegacyIcon.MOD_TIME_CHANGER,
    ) {
    @Property(type = PropertyType.NUMBER, translate = TranslateText.TIME, min = 0.0, max = 24.0, current = 12.0)
    @JvmField
    var timeSetting = 12.0

    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: TimeChangerMod? = null
    }

    fun getTimeSetting(): NumberSetting? = getNumberSetting(this, "timeSetting")
}
