package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventFireOverlay
import com.shindoclient.shindo.management.event.impl.EventRenderPumpkinOverlay
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType

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
