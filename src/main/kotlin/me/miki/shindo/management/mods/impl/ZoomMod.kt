package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventScrollMouse
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.event.impl.EventZoomFov
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import org.lwjgl.input.Keyboard

class ZoomMod : Mod(TranslateText.ZOOM, TranslateText.ZOOM_DESCRIPTION, ModCategory.PLAYER, LegacyIcon.MOD_ZOOM) {
    private val zoomAnimation = SimpleAnimation()

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SCROLL)
    private val scrollSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SMOOTH_ZOOM)
    private val smoothZoomSetting = false

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.ZOOM_SPEED,
        min = 5.0,
        max = 2.00,
        step = 1.0,
        current = 14.0
    )
    private val zoomSpeedSetting = 14.0

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.ZOOM_FACTOR,
        min = 2.0,
        max = 1.05,
        step = 1.0,
        current = 4.0
    )
    private val factorSetting = 4.0

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SMOOTH_CAMERA)
    private val smoothCameraSetting = true

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, keyCode = Keyboard.KEY_C)
    private val zoomKey = Keyboard.KEY_C

    var wasCinematic: Boolean = false
    private var active = false
    private var lastSensitivity = 0f
    private var currentFactor = 1f

    @EventTarget
    fun onTick(event: EventTick?) {
        if (Keyboard.isKeyDown(zoomKey)) {
            if (!active) {
                active = true
                lastSensitivity = mc.gameSettings.mouseSensitivity
                resetFactor()
                wasCinematic = this.mc.gameSettings.smoothCamera
                mc.gameSettings.smoothCamera = smoothCameraSetting
                mc.renderGlobal.setDisplayListEntitiesDirty()
            }
        } else if (active) {
            active = false
            setFactor(1f)
            mc.gameSettings.mouseSensitivity = lastSensitivity
            mc.gameSettings.smoothCamera = wasCinematic
        }
    }

    @EventTarget
    fun onFov(event: EventZoomFov) {
        zoomAnimation.setAnimation(currentFactor, zoomSpeedSetting.toFloat().toDouble())

        event.setFov(event.getFov() * (if (smoothZoomSetting) zoomAnimation.value else currentFactor))
    }

    @EventTarget
    fun onScroll(event: EventScrollMouse) {
        if (active && scrollSetting) {
            event.setCancelled(true)
            if (event.getAmount() < 0) {
                if (currentFactor < 0.98) {
                    currentFactor += 0.03f
                }
            } else if (event.getAmount() > 0) {
                if (currentFactor > 0.06) {
                    currentFactor -= 0.03f
                }
            }
        }
    }

    fun resetFactor() {
        setFactor(1 / factorSetting.toFloat())
    }

    fun setFactor(factor: Float) {
        if (factor != currentFactor) {
            mc.renderGlobal.setDisplayListEntitiesDirty()
        }
        currentFactor = factor
    }
}




