package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getNumberSetting

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
