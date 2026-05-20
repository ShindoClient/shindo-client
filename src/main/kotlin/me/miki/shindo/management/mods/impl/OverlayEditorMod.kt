package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventFireOverlay
import me.miki.shindo.management.event.impl.EventRenderPumpkinOverlay
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class OverlayEditorMod :
    Mod(
        TranslateText.OVERLAY_EDITOR,
        TranslateText.OVERLAY_EDITOR_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_OVERLAY_EDITOR,
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HIDE_PUMPKIN)
    private val hidePumpkinSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HIDE_FIRE)
    private val hideFireSetting = false

    @EventTarget
    fun onRenderPumpkinOverlay(event: EventRenderPumpkinOverlay) {
        event.setCancelled(hidePumpkinSetting)
    }

    @EventTarget
    fun onFireOverlay(event: EventFireOverlay) {
        event.setCancelled(hideFireSetting)
    }
}
