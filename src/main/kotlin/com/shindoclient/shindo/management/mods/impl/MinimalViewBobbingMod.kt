package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic

class MinimalViewBobbingMod :
    Mod(
        TranslateText.MINIMAL_VIEW_BOBBING,
        TranslateText.MINIMAL_VIEW_BOBBING_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_MINIMAL_VIEW_BOBBING,
    ) {
    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: MinimalViewBobbingMod? = null
    }
}
