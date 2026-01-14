package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.client.Minecraft

class FPSDisplayMod :
    SimpleHUDMod(TranslateText.FPS_DISPLAY, TranslateText.FPS_DISPLAY_DESCRIPTION, LegacyIcon.MOD_FPS_DISPLAY) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconEnabled = true

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        this.draw()
    }

    public override fun getText(): String? {
        return Minecraft.getDebugFPS().toString() + " FPS"
    }

    public override fun getIcon(): String? {
        return if (iconEnabled) LegacyIcon.MONITOR else null
    }
}


