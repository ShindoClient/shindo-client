package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Keyboard

class ToggleSprintMod :
    SimpleHUDMod(TranslateText.TOGGLE_SPRINT, TranslateText.TOGGLE_SPRINT_DESCRIPTION, LegacyIcon.MOD_TOGGLE_SPRINT) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HUD)
    private val hudEnabled = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ALWAYS)
    private val alwaysSprint = false

    private var startTime: Long = 0
    private var wasDown = false

    private var state: State = State.WALK

    public override fun setup() {
        state = State.WALK
    }

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        if (hudEnabled) {
            this.draw()
        }

        this.setDraggable(hudEnabled)
    }

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        KeyBinding.setKeyBindState(
            mc.gameSettings.keyBindSprint.getKeyCode(),
            state == State.HELD || state == State.TOGGLED || alwaysSprint
        )
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        val down = Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode())

        if (alwaysSprint || mc.currentScreen != null) {
            return
        }

        if (down) {
            if (!wasDown) {
                startTime = System.currentTimeMillis()

                if (state == State.TOGGLED) {
                    state = State.HELD
                } else {
                    state = State.TOGGLED
                }
            } else if ((System.currentTimeMillis() - startTime) > 250) {
                state = State.HELD
            }
        } else if (state == State.HELD && mc.thePlayer.isSprinting()) {
            state = State.VANILLA
        } else if ((state == State.VANILLA || state == State.HELD) && !mc.thePlayer.isSprinting()) {
            state = State.WALK
        }

        wasDown = down
    }

    public override fun getText(): String? {
        val prefix = "Sprinting"

        if (alwaysSprint) {
            return prefix + " (Always)"
        }

        if (state == State.WALK) {
            return "Walking"
        }

        return prefix + " (" + state.displayName + ")"
    }

    private enum class State(val displayName: String) {
        WALK("Walking"),
        VANILLA("Vanilla"),
        HELD("Key Held"),
        TOGGLED("Toggled")
    }
}



