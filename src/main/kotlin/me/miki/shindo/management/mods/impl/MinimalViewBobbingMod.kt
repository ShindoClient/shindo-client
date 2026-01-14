package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon

class MinimalViewBobbingMod : Mod(
    TranslateText.MINIMAL_VIEW_BOBBING,
    TranslateText.MINIMAL_VIEW_BOBBING_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_MINIMAL_VIEW_BOBBING
) {
    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: MinimalViewBobbingMod? = null
    }
}




