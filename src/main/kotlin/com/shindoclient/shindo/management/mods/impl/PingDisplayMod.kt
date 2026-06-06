package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventNVG
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.SimpleHUDMod
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.utils.ServerUtils.getPing

class PingDisplayMod : SimpleHUDMod(TranslateText.PING_DISPLAY, TranslateText.PING_DISPLAY_DESCRIPTION, Shinconic.MOD_PING_DISPLAY) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconEnabled = true

    @EventTarget
    fun onRender2D(event: EventNVG) {
        this.draw()
    }

    override fun getText(): String = getPing().toString() + " ms"

    override fun getIcon(): String? = if (iconEnabled) Lucide.BAR_CHART else null
}
