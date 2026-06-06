package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic

class NametagMod : Mod(TranslateText.NAMETAG, TranslateText.NAMETAG_DESCRIPTION, ModCategory.PLAYER, Shinconic.MOD_NAMETAG) {
    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: NametagMod? = null
    }
}
