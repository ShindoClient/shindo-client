package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventTick
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyEnum
import com.shindoclient.shindo.management.settings.config.PropertyType
import org.lwjgl.input.Keyboard

class TaplookMod : Mod(TranslateText.TAPLOOK, TranslateText.TAPLOOK_DESCRIPTION, ModCategory.PLAYER, Shinconic.MOD_TAPLOOK) {
    @Property(type = PropertyType.COMBO, translate = TranslateText.PERSPECTIVE)
    private val perspective = Perspective.FRONT

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, keyCode = Keyboard.KEY_P)
    private val keybindSetting = Keyboard.KEY_P

    private var active = false
    private var prevPerspective = 0

    @EventTarget
    fun onTick(event: EventTick?) {
        if (Keyboard.isKeyDown(keybindSetting)) {
            if (!active) {
                this.start()
            }
        } else if (active) {
            this.stop()
        }
    }

    private fun start() {
        val perspectiveView = if (perspective == Perspective.FRONT) 2 else 1

        active = true
        prevPerspective = mc.gameSettings.thirdPersonView
        mc.gameSettings.thirdPersonView = perspectiveView
        mc.renderGlobal.setDisplayListEntitiesDirty()
    }

    private fun stop() {
        active = false
        mc.gameSettings.thirdPersonView = prevPerspective
        mc.renderGlobal.setDisplayListEntitiesDirty()
    }

    private enum class Perspective(
        private val translate: TranslateText,
    ) : PropertyEnum {
        FRONT(TranslateText.FRONT),
        BEHIND(TranslateText.BEHIND),
        ;

        override fun getTranslate(): TranslateText = translate
    }
}
