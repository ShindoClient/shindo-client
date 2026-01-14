package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.metadata.SettingRegistry.getNumberSetting

class FPSSpooferMod : Mod(
    TranslateText.FPS_SPOOFER,
    TranslateText.FPS_SPOOFER_DESCRIPTION,
    ModCategory.OTHER,
    LegacyIcon.MOD_FPS_SPOOFER,
    "fake"
) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.MULTIPLIER,
        min = 1,
        max = 30,
        current = 2,
        step = 1
    )
    val multiplierSetting: Int
        get() = getNumberSetting(this, "multiplierSetting")

    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: FPSSpooferMod? = null
    }
}




