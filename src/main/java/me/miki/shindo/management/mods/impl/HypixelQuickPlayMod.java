package me.miki.shindo.management.mods.impl;

import me.miki.shindo.gui.GuiQuickPlay;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventKey;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import org.lwjgl.input.Keyboard;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class HypixelQuickPlayMod extends Mod {

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, keyCode = Keyboard.KEY_N)
    private int keybindSetting = Keyboard.KEY_N;

    public HypixelQuickPlayMod() {
        super(TranslateText.HYPIXEL_QUICK_PLAY, TranslateText.HYPIXEL_QUICK_PLAY_DESCRIPTION, ModCategory.PLAYER);
    }

    @EventTarget
    public void onKey(EventKey event) {

        if (event.getKeyCode() == keybindSetting) {
            mc.displayGuiScreen(new GuiQuickPlay());
        }
    }
}
