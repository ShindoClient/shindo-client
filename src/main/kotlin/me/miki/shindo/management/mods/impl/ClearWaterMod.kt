package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventWaterOverlay
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon

class ClearWaterMod : Mod(
    TranslateText.CLEAR_WATER,
    TranslateText.CLEAR_WATER_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_CLEAR_WATER
) {
    @EventTarget
    fun onWaterOverlay(event: EventWaterOverlay) {
        event.setCancelled(true)
    }
}




