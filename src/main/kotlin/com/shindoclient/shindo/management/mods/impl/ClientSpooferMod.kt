package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyEnum
import com.shindoclient.shindo.management.settings.config.PropertyType

class ClientSpooferMod :
    Mod(
        TranslateText.CLIENT_SPOOFER,
        TranslateText.CLIENT_SPOOFER_DESCRIPTION,
        ModCategory.OTHER,
        Shinconic.MOD_CLIENT_SPOOFER,
    ) {
    @Property(type = PropertyType.COMBO, translate = TranslateText.TYPE)
    @JvmField
    val spoofType: SpoofType = SpoofType.VANILLA

    init {
        instance = this
    }

    enum class SpoofType(
        private val translate: TranslateText,
    ) : PropertyEnum {
        VANILLA(TranslateText.VANILLA),
        FORGE(TranslateText.FORGE),
        ;

        override fun getTranslate(): TranslateText = translate
    }

    companion object {
        @JvmField
        var instance: ClientSpooferMod? = null
    }
}
