package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventNVG
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.SimpleHUDMod
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.utils.ServerUtils.getServerIP

class ServerIPDisplayMod :
    SimpleHUDMod(
        TranslateText.SERVER_IP,
        TranslateText.SERVER_IP_DISPLAY_DESCRIPTION,
        Shinconic.MOD_SERVER_IP_DISPLAY,
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val showIcon = true

    @EventTarget
    fun onRender2D(event: EventNVG) {
        this.draw()
    }

    override fun getText(): String = getServerIP()

    override fun getIcon(): String? = if (showIcon) Lucide.SERVER else null
}
