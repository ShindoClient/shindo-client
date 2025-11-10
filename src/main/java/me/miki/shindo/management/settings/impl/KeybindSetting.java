package me.miki.shindo.management.settings.impl;

import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

public class KeybindSetting extends Setting {

    private final int defaultKeyCode;
    private int keyCode;

    public KeybindSetting(TranslateText text, ConfigOwner parent, int keyCode) {
        super(text, parent);
        this.defaultKeyCode = keyCode;
        this.keyCode = keyCode;
    }

    public KeybindSetting(String name, ConfigOwner parent, int keyCode) {
        super(name, parent);
        this.defaultKeyCode = keyCode;
        this.keyCode = keyCode;
    }

    @Override
    public void reset() {
        this.keyCode = defaultKeyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getDefaultKeyCode() {
        return defaultKeyCode;
    }

    public boolean isKeyDown() {
        return Keyboard.isKeyDown(keyCode) && !(Minecraft.getMinecraft().currentScreen instanceof Gui);
    }
}
