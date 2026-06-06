package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventHurtCamera
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType

class MinimalDamageShakeMod :
    Mod(
        TranslateText.MINIMAL_DAMAGE_SHAKE,
        TranslateText.MINIMAL_DAMAGE_SHAKE_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_MINIMAL_DAMAGE_SHAKE,
        "nohurtcam",
    ) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.INTENSITY,
        min = 0.0,
        max = 100.0,
        current = 0.0,
        step = 1.0,
    )
    private val intensitySetting = 0

    @EventTarget
    fun onHurtCamera(event: EventHurtCamera) {
        event.intensity = intensitySetting / 100f
    }
}
