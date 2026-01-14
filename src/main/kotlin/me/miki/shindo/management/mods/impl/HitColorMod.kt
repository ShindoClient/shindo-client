package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventHitOverlay
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import java.awt.Color

class HitColorMod :
    Mod(TranslateText.HIT_COLOR, TranslateText.HIT_COLOR_DESCRIPTION, ModCategory.RENDER, LegacyIcon.MOD_HIT_COLOR) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CUSTOM_COLOR)
    private val customColorSetting = false

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR, color = -0x10000)
    private val colorSetting = Color(255, 0, 0)

    @Property(type = PropertyType.NUMBER, translate = TranslateText.ALPHA, min = 0, max = 1, current = 0.45)
    private val alphaSetting = 0.45

    @EventTarget
    fun onHitOverlay(event: EventHitOverlay) {
        val currentColor = getInstance().colorManager.getCurrentColor()
        val lastColor = if (customColorSetting) colorSetting else currentColor.getInterpolateColor()

        event.setRed(lastColor.getRed() / 255f)
        event.setGreen(lastColor.getGreen() / 255f)
        event.setBlue(lastColor.getBlue() / 255f)
        event.setAlpha(alphaSetting.toFloat())
    }
}




