package me.miki.shindo.management.mods.impl;

import me.miki.shindo.gui.GuiQuickPlay;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventKey;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.KeybindSetting;
import org.lwjgl.input.Keyboard;

public class HypixelQuickPlayMod extends Mod {

    private final KeybindSetting keybindSetting = new KeybindSetting(TranslateText.KEYBIND, this, Keyboard.KEY_N);

    public HypixelQuickPlayMod() {
        super(TranslateText.HYPIXEL_QUICK_PLAY, TranslateText.HYPIXEL_QUICK_PLAY_DESCRIPTION, ModCategory.PLAYER);
    }

    @EventTarget
    public void onKey(EventKey event) {

        if (event.getKeyCode() == keybindSetting.getKeyCode()) {
            mc.displayGuiScreen(new GuiQuickPlay());
        }
    }
}
