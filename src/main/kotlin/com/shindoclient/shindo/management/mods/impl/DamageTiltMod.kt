package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic

class DamageTiltMod :
    Mod(
        TranslateText.DAMAGE_TILT,
        TranslateText.DAMAGE_TILT_DESCRIPTION,
        ModCategory.PLAYER,
        Shinconic.MOD_DAMAGE_TILT,
    ) {
    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: DamageTiltMod? = null
    }
}
