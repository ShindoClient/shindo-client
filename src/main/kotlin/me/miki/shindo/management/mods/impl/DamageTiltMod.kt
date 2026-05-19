package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic

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
