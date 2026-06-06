package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.settings.impl.NumberSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getNumberSetting

class FPSSpooferMod :
    Mod(
        TranslateText.FPS_SPOOFER,
        TranslateText.FPS_SPOOFER_DESCRIPTION,
        ModCategory.OTHER,
        Shinconic.MOD_FPS_SPOOFER,
        "fake",
    ) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.MULTIPLIER,
        min = 1.0,
        max = 30.0,
        step = 1.0,
    )
    private val multiplierSetting = 2.0

    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: FPSSpooferMod? = null
    }

    fun getMultiplierSetting(): NumberSetting? = getNumberSetting(this, "multiplierSetting")
}
