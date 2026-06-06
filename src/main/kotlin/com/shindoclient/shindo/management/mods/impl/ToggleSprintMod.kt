package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventNVG
import com.shindoclient.shindo.management.event.impl.EventTick
import com.shindoclient.shindo.management.event.impl.EventUpdate
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.SimpleHUDMod
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Keyboard

class ToggleSprintMod : SimpleHUDMod(TranslateText.TOGGLE_SPRINT, TranslateText.TOGGLE_SPRINT_DESCRIPTION, Shinconic.MOD_TOGGLE_SPRINT) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HUD)
    private val hudEnabled = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ALWAYS)
    private val alwaysSprint = false

    private var startTime: Long = 0
    private var wasDown = false

    private var state: State = State.WALK

    override fun setup() {
        state = State.WALK
    }

    @EventTarget
    fun onRender2D(event: EventNVG) {
        if (hudEnabled) {
            this.draw()
        }

        this.setDraggable(hudEnabled)
    }

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        KeyBinding.setKeyBindState(
            mc.gameSettings.keyBindSprint.keyCode,
            state == State.HELD || state == State.TOGGLED || alwaysSprint,
        )
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        val down = Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.keyCode)

        if (alwaysSprint || mc.currentScreen != null) {
            return
        }

        if (down) {
            if (!wasDown) {
                startTime = System.currentTimeMillis()

                state =
                    if (state == State.TOGGLED) {
                        State.HELD
                    } else {
                        State.TOGGLED
                    }
            } else if ((System.currentTimeMillis() - startTime) > 250) {
                state = State.HELD
            }
        } else if (state == State.HELD && mc.thePlayer.isSprinting) {
            state = State.VANILLA
        } else if ((state == State.VANILLA || state == State.HELD) && !mc.thePlayer.isSprinting) {
            state = State.WALK
        }

        wasDown = down
    }

    override fun getText(): String {
        val prefix = "Sprinting"

        if (alwaysSprint) {
            return "$prefix (Always)"
        }

        if (state == State.WALK) {
            return "Walking"
        }

        return prefix + " (" + state.displayName + ")"
    }

    private enum class State(
        val displayName: String,
    ) {
        WALK("Walking"),
        VANILLA("Vanilla"),
        HELD("Key Held"),
        TOGGLED("Toggled"),
    }
}
