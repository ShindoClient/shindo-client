package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon

class HitDelayFixMod : Mod(
    TranslateText.HIT_DELAY_FIX,
    TranslateText.HIT_DELAY_FIX_DESCRIPTION,
    ModCategory.PLAYER,
    LegacyIcon.MOD_HIT_DELAY_FIX,
    "nodelay",
    true
) {
    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: HitDelayFixMod? = null
    }
}




