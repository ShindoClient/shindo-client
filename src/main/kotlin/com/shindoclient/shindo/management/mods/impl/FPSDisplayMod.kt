package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventNVG
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.SimpleHUDMod
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import net.minecraft.client.Minecraft

class FPSDisplayMod : SimpleHUDMod(TranslateText.FPS_DISPLAY, TranslateText.FPS_DISPLAY_DESCRIPTION, Shinconic.MOD_FPS_DISPLAY) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconEnabled = true

    @EventTarget
    fun onRender2D(event: EventNVG?) {
        this.draw()
    }

    override fun getText(): String = Minecraft.getDebugFPS().toString() + " FPS"

    override fun getIcon(): String? = if (iconEnabled) Lucide.MONITOR else null
}
