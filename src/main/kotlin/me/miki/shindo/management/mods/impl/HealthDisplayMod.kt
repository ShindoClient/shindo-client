package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class HealthDisplayMod :
    SimpleHUDMod(
        TranslateText.HEALTH_DISPLAY,
        TranslateText.HEALTH_DISPLAY_DESCRIPTION,
        Shinconic.MOD_HEALTH_DISPLAY,
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconEnabled = true

    @EventTarget
    fun onRender2D(event: EventNVG?) {
        this.draw()
    }

    override fun getText(): String =
        mc.thePlayer.health
            .toInt()
            .toString() + " Health"

    override fun getIcon(): String? = if (iconEnabled) Lucide.HEART else null
}
