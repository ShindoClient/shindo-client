package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.settings.impl.NumberSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getNumberSetting

class TimeChangerMod :
    Mod(
        TranslateText.TIME_CHANGER,
        TranslateText.TIME_CHANGER_DESCRIPTION,
        ModCategory.WORLD,
        Shinconic.MOD_TIME_CHANGER,
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
