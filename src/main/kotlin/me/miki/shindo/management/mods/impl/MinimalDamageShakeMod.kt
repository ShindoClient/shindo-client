package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventHurtCamera
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class MinimalDamageShakeMod : Mod(
    TranslateText.MINIMAL_DAMAGE_SHAKE,
    TranslateText.MINIMAL_DAMAGE_SHAKE_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_MINIMAL_DAMAGE_SHAKE,
    "nohurtcam"
) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.INTENSITY,
        min = 0.0,
        max = 10.00,
        current = 0.0,
        step = 1.0
    )
    private val intensitySetting = 0

    @EventTarget
    fun onHurtCamera(event: EventHurtCamera) {
        event.setIntensity(intensitySetting / 100f)
    }
}




