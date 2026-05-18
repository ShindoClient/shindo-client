package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventCameraRotation
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class FarCameraMod :
    Mod(TranslateText.FAR_CAMERA, TranslateText.FAR_CAMERA_DESCRIPTION, ModCategory.RENDER, LegacyIcon.MOD_FAR_CAMERA) {
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
