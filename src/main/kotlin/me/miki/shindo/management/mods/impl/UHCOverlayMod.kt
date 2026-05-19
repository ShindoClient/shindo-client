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

class UHCOverlayMod :
    Mod(
        TranslateText.UHC_OVERLAY,
        TranslateText.UHC_OVERLAY_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_UHC_OVERLAY,
    ) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.GOLD_INGOT_SCALE,
        min = 1.0,
        max = 5.0,
        current = 1.5,
    )
    @JvmField
    var goldIngotScaleSetting = 1.5

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.GOLD_NUGGET_SCALE,
        min = 1.0,
        max = 5.0,
        current = 1.5,
    )
    @JvmField
    var goldNuggetScaleSetting = 1.5

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.GOLD_ORE_SCALE,
        min = 1.0,
        max = 5.0,
        current = 1.5,
    )
    @JvmField
    var goldOreScaleSetting = 1.5

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.GOLD_APPLE_SCALE,
        min = 1.0,
        max = 5.0,
        current = 1.5,
    )
    @JvmField
    var goldAppleScaleSetting = 1.5

    @Property(type = PropertyType.NUMBER, translate = TranslateText.SKULL_SCALE, min = 1.0, max = 5.0, current = 1.5)
    @JvmField
    var skullScaleSetting = 1.5

    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: UHCOverlayMod? = null
    }

    fun getGoldIngotScaleSetting(): NumberSetting? = getNumberSetting(this, "goldIngotScaleSetting")

    fun getGoldNuggetScaleSetting(): NumberSetting? = getNumberSetting(this, "goldNuggetScaleSetting")

    fun getGoldOreScaleSetting(): NumberSetting? = getNumberSetting(this, "goldOreScaleSetting")

    fun getGoldAppleScaleSetting(): NumberSetting? = getNumberSetting(this, "goldAppleScaleSetting")

    fun getSkullScaleSetting(): NumberSetting? = getNumberSetting(this, "skullScaleSetting")
}
