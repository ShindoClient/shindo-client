package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.gui.GuiQuickPlay
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventKey
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import org.lwjgl.input.Keyboard

class HypixelQuickPlayMod :
    Mod(
        TranslateText.HYPIXEL_QUICK_PLAY,
        TranslateText.HYPIXEL_QUICK_PLAY_DESCRIPTION,
        ModCategory.PLAYER,
        Shinconic.MOD_HYPIXEL_QUICK_PLAY,
    ) {
    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, keyCode = Keyboard.KEY_N)
    private val keybindSetting = Keyboard.KEY_N

    @EventTarget
    fun onKey(event: EventKey) {
        if (event.getKeyCode() == keybindSetting) {
            mc.displayGuiScreen(GuiQuickPlay())
        }
    }
}
