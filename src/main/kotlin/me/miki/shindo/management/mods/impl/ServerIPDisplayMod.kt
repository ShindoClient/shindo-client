package me.miki.shindo.management.mods.impl

import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.ServerUtils.getServerIP

class ServerIPDisplayMod : SimpleHUDMod(
    TranslateText.SERVER_IP,
    TranslateText.SERVER_IP_DISPLAY_DESCRIPTION,
    LegacyIcon.MOD_SERVER_IP_DISPLAY
) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val showIcon = true

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        this.draw()
    }

    override fun getText(): String {
        return getServerIP()
    }

    override fun getIcon(): String? {
        return if (showIcon) LegacyIcon.SERVER else null
    }
}


