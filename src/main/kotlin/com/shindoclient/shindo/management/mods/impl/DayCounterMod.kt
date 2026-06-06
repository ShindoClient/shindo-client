package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventNVG
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.SimpleHUDMod
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType

class DayCounterMod : SimpleHUDMod(TranslateText.DAY_COUNTER, TranslateText.DAY_COUNTER_DESCRIPTION, Shinconic.MOD_DAY_COUNTER) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @EventTarget
    fun onRender2D(event: EventNVG?) {
        this.draw()
    }

    override fun getText(): String {
        val time = mc.theWorld.getWorldInfo().worldTotalTime / 24000L

        return time.toString() + " Day" + (if (time != 1L) "s" else "")
    }

    override fun getIcon(): String? = if (iconSetting) Lucide.SUNRISE else null
}
