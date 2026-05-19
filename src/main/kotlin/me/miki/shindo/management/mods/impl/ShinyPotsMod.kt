package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic

class ShinyPotsMod : Mod(TranslateText.SHINY_POTS, TranslateText.SHINY_POTS_DESCRIPTION, ModCategory.RENDER, Shinconic.MOD_SHINY_POTS) {
    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: ShinyPotsMod? = null
    }
}
