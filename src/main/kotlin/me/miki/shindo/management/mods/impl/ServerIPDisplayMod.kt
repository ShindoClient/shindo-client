package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.ServerUtils.getServerIP

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
