package me.miki.shindo.management.mods.impl

import me.miki.shindo.gui.GuiQuickPlay
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventKey
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
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
        if (event.keyCode == keybindSetting) {
            mc.displayGuiScreen(GuiQuickPlay())
        }
    }
}
