package me.miki.shindo.management.mods.impl

import me.miki.shindo.gui.GuiAutoTextManager
import me.miki.shindo.management.autotext.AutoTextManager
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventKey
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import org.lwjgl.input.Keyboard

class AutoTextMod : Mod(
    TranslateText.AUTO_TEXT,
    TranslateText.AUTO_TEXT_DESCRIPTION,
    ModCategory.PLAYER,
    LegacyIcon.MOD_AUTO_TEXT,
    "messagetexthotkeymacro"
) {
    val autoTextManager = AutoTextManager()

    init {
        instance = this
    }

    override fun setup() {
        setHide(false)
    }

    fun openManager() {
        mc.displayGuiScreen(GuiAutoTextManager(null))
    }

    @EventTarget
    fun onKey(event: EventKey) {
        if (!isToggled()) {
            return
        }
        if (mc.thePlayer == null || mc.currentScreen != null) {
            return
        }

        for (entry in autoTextManager.getEntries()) {
            if (entry.keyCode == Keyboard.KEY_NONE) {
                continue
            }
            if (event.getKeyCode() != entry.keyCode) {
                continue
            }
            if (entry.textOrCommand.isBlank()) {
                continue
            }
            mc.thePlayer.sendChatMessage(entry.textOrCommand)
        }
    }

    companion object {
        lateinit var instance: AutoTextMod
            private set
    }
}

