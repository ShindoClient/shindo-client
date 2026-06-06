package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventHitOverlay
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import java.awt.Color

class HitColorMod : Mod(TranslateText.HIT_COLOR, TranslateText.HIT_COLOR_DESCRIPTION, ModCategory.RENDER, Shinconic.MOD_HIT_COLOR) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CUSTOM_COLOR)
    private val customColorSetting = false

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR, color = -0x10000)
    private val colorSetting = Color(255, 0, 0)

    @Property(type = PropertyType.NUMBER, translate = TranslateText.ALPHA, min = 0.0, max = 1.0, current = 0.45)
    private val alphaSetting = 0.45

    @EventTarget
    fun onHitOverlay(event: EventHitOverlay) {
        val currentColor = Shindo.getInstance().getColorManager().getCurrentColor()
        val lastColor = if (customColorSetting) colorSetting else currentColor.getInterpolateColor()

        event.red = lastColor.red / 255f
        event.green = lastColor.green / 255f
        event.blue = lastColor.blue / 255f
        event.alpha = alphaSetting.toFloat()
    }
}
