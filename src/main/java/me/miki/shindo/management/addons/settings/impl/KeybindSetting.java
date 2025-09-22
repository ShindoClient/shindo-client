package me.miki.shindo.management.addons.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.settings.AddonSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

@Getter
public class KeybindSetting extends AddonSetting {

    private final int defaultKeyCode;

    @Setter
    private int keyCode;

    public KeybindSetting(String text, Addon parent, int keyCode) {
        super(text, parent);
        this.defaultKeyCode = keyCode;
        this.keyCode = keyCode;

        Shindo.getInstance().getAddonManager().addSettings(this);
    }

    @Override
    public void reset() {
        this.keyCode = defaultKeyCode;
    }

    public boolean isKeyDown() {
        return Keyboard.isKeyDown(keyCode) && !(Minecraft.getMinecraft().currentScreen instanceof Gui);
    }
}