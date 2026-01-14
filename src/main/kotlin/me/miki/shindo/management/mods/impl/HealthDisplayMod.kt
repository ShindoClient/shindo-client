package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class HealthDisplayMod : SimpleHUDMod(
    TranslateText.HEALTH_DISPLAY,
    TranslateText.HEALTH_DISPLAY_DESCRIPTION,
    LegacyIcon.MOD_HEALTH_DISPLAY
) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconEnabled = true

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        this.draw()
    }

    public override fun getText(): String? {
        return mc.thePlayer.getHealth().toInt().toString() + " Health"
    }

    public override fun getIcon(): String? {
        return if (iconEnabled) LegacyIcon.HEART else null
    }
}


