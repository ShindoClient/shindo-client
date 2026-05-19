package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

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
