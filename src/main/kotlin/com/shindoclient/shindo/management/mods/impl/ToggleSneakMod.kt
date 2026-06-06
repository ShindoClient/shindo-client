package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventKey
import com.shindoclient.shindo.management.event.impl.EventUpdate
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import net.minecraft.client.gui.Gui
import net.minecraft.client.settings.KeyBinding

class ToggleSneakMod :
    Mod(
        TranslateText.TOGGLE_SNEAK,
        TranslateText.TOGGLE_SNEAK_DESCRIPTION,
        ModCategory.PLAYER,
        Shinconic.MOD_TOGGLE_SNEAK,
    ) {
    private var toggle = false

    override fun setup() {
        toggle = false
    }

    @EventTarget
    fun onKey(event: EventKey) {
        if (event.getKeyCode() == mc.gameSettings.keyBindSneak.keyCode) {
            toggle = !toggle
        }
    }

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        if (mc.currentScreen is Gui) {
            setSneak(false)
        } else {
            setSneak(toggle)
        }
    }

    override fun onDisable() {
        super.onDisable()
        toggle = false
        setSneak(false)
    }

    private fun setSneak(state: Boolean) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.keyCode, state)
    }
}
