package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventNVG
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.SimpleHUDMod
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType

class PlayTimeDisplayMod :
    SimpleHUDMod(
        TranslateText.PLAY_TIME_DISPLAY,
        TranslateText.PLAY_TIME_DISPLAY_DESCRIPTION,
        Shinconic.MOD_PLAY_TIME_DISPLAY,
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @EventTarget
    fun onRender2D(event: EventNVG) {
        this.draw()
    }

    override fun getText(): String {
        var sec = ((System.currentTimeMillis() - Shindo.getInstance().getShindoAPI().launchTime) / 1000).toInt()
        val min = (sec % 3600) / 60
        val hour = sec / 3600
        sec %= 60

        return String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec)
    }

    override fun getIcon(): String? = if (iconSetting) Lucide.CLOCK else null
}
