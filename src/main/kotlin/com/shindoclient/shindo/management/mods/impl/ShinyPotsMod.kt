package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic

class ShinyPotsMod : Mod(TranslateText.SHINY_POTS, TranslateText.SHINY_POTS_DESCRIPTION, ModCategory.RENDER, Shinconic.MOD_SHINY_POTS) {
    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: ShinyPotsMod? = null
    }
}
