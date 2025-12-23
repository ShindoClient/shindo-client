package me.miki.shindo.management.settings.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.config.ConfigOwner
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import org.lwjgl.input.Keyboard

open class KeybindSetting : Setting {

    private val defaultKeyCode: Int
    private var keyCode: Int

    constructor(text: TranslateText, parent: ConfigOwner, keyCode: Int) : super(text, parent) {
        this.defaultKeyCode = keyCode
        this.keyCode = keyCode
    }

    constructor(name: String, parent: ConfigOwner, keyCode: Int) : super(name, parent) {
        this.defaultKeyCode = keyCode
        this.keyCode = keyCode
    }

    override fun reset() {
        keyCode = defaultKeyCode
    }

    fun getKeyCode(): Int {
        return keyCode
    }

    open fun setKeyCode(keyCode: Int) {
        this.keyCode = keyCode
    }

    fun getDefaultKeyCode(): Int {
        return defaultKeyCode
    }

    fun isKeyDown(): Boolean {
        return Keyboard.isKeyDown(keyCode) && Minecraft.getMinecraft().currentScreen !is Gui
    }
}
