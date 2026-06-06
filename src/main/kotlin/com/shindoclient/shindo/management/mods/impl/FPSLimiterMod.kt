package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.settings.impl.BooleanSetting
import com.shindoclient.shindo.management.settings.impl.NumberSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getNumberSetting

class FPSLimiterMod :
    Mod(
        TranslateText.FPS_LIMITER,
        TranslateText.FPS_LIMITER_DESCRIPTION,
        ModCategory.OTHER,
        Shinconic.MOD_FPS_LIMITER,
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.LIMIT_MAX_FPS)
    @JvmField
    var limitMaxFpsSetting = false

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.MAX_FPS,
        min = 240.0,
        max = 1440.0,
        step = 1.0,
        current = 480.0,
    )
    @JvmField
    var maxFpsSetting = 480.0

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.LIMIT_GUI_FPS)
    @JvmField
    var limitGuiFps = false

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.GUI_FPS,
        min = 1.0,
        max = 24.00,
        step = 1.0,
        current = 30.0,
    )
    @JvmField
    var guiFpsSetting = 30.0

    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: FPSLimiterMod? = null
    }

    fun getLimitMaxFpsSetting(): BooleanSetting? = getBooleanSetting(this, "limitMaxFpsSetting")

    fun getMaxFpsSetting(): NumberSetting? = getNumberSetting(this, "maxFpsSetting")

    fun getLimitGuiFpsSetting(): BooleanSetting? = getBooleanSetting(this, "limitGuiFps")

    fun getGuiFpsSetting(): NumberSetting? = getNumberSetting(this, "guiFpsSetting")

    fun getLimitGuiFps(): BooleanSetting? = getBooleanSetting(this, "limitGuiFps")
}
