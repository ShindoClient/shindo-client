package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic

class HitDelayFixMod :
    Mod(
        TranslateText.HIT_DELAY_FIX,
        TranslateText.HIT_DELAY_FIX_DESCRIPTION,
        ModCategory.PLAYER,
        Shinconic.MOD_HIT_DELAY_FIX,
        "nodelay",
        true,
    ) {
    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: HitDelayFixMod? = null
    }
}
