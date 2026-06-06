package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.gui.GuiAutoTextManager
import com.shindoclient.shindo.management.autotext.AutoTextManager
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventKey
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import org.lwjgl.input.Keyboard

class AutoTextMod :
    Mod(
        TranslateText.AUTO_TEXT,
        TranslateText.AUTO_TEXT_DESCRIPTION,
        ModCategory.PLAYER,
        Shinconic.MOD_AUTO_TEXT,
        "messagetexthotkeymacro",
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
