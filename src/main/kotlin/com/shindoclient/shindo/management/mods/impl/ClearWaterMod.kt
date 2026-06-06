package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventWaterOverlay
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic

class ClearWaterMod :
    Mod(
        TranslateText.CLEAR_WATER,
        TranslateText.CLEAR_WATER_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_CLEAR_WATER,
    ) {
    @EventTarget
    fun onWaterOverlay(event: EventWaterOverlay) {
        event.setCancelled(true)
    }
}
