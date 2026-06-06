package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventCameraRotation
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType

class FarCameraMod : Mod(TranslateText.FAR_CAMERA, TranslateText.FAR_CAMERA_DESCRIPTION, ModCategory.RENDER, Shinconic.MOD_FAR_CAMERA) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.RANGE,
        min = 0.0,
        max = 50.0,
        current = 15.0,
        step = 1.0,
    )
    private val rangeSetting = 15

    @EventTarget
    fun onCameraRotation(event: EventCameraRotation) {
        event.thirdPersonDistance = rangeSetting.toFloat()
    }
}
