package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Shinconic

class NametagMod : Mod(TranslateText.NAMETAG, TranslateText.NAMETAG_DESCRIPTION, ModCategory.PLAYER, Shinconic.MOD_NAMETAG) {
    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: NametagMod? = null
    }
}
