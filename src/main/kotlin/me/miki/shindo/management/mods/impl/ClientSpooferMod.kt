package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType

class ClientSpooferMod : Mod(
    TranslateText.CLIENT_SPOOFER,
    TranslateText.CLIENT_SPOOFER_DESCRIPTION,
    ModCategory.OTHER,
    LegacyIcon.MOD_CLIENT_SPOOFER
) {
    @Property(type = PropertyType.COMBO, translate = TranslateText.TYPE)
    @JvmField
    val spoofType: SpoofType = SpoofType.VANILLA

    init {
        instance = this
    }

    enum class SpoofType(private val translate: TranslateText) : PropertyEnum {
        VANILLA(TranslateText.VANILLA),
        FORGE(TranslateText.FORGE);

        override fun getTranslate(): TranslateText {
            return translate
        }
    }

    companion object {
        @JvmField
        var instance: ClientSpooferMod? = null
    }
}




