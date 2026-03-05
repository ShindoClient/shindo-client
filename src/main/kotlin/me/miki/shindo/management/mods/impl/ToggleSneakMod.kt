package me.miki.shindo.management.mods.impl

import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.EventKey
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import net.minecraft.client.gui.Gui
import net.minecraft.client.settings.KeyBinding

class ToggleSneakMod : Mod(
    TranslateText.TOGGLE_SNEAK,
    TranslateText.TOGGLE_SNEAK_DESCRIPTION,
    ModCategory.PLAYER,
    LegacyIcon.MOD_TOGGLE_SNEAK
) {
    private var toggle = false

    override fun setup() {
        toggle = false
    }

    @EventTarget
    fun onKey(event: EventKey) {
        if (event.keyCode == mc.gameSettings.keyBindSneak.keyCode) {
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




