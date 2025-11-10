package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventKey;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import org.lwjgl.input.Keyboard;

public class AutoTextMod extends Mod {

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.TEXT_1_KEY, keyCode = Keyboard.KEY_NONE)
    private int text1KeybindSetting = Keyboard.KEY_NONE;

    @Property(type = PropertyType.TEXT, translate = TranslateText.TEXT_1, text = "")
    private String text1Setting = "";

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.TEXT_2_KEY, keyCode = Keyboard.KEY_NONE)
    private int text2KeybindSetting = Keyboard.KEY_NONE;

    @Property(type = PropertyType.TEXT, translate = TranslateText.TEXT_2, text = "")
    private String text2Setting = "";

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.TEXT_3_KEY, keyCode = Keyboard.KEY_NONE)
    private int text3KeybindSetting = Keyboard.KEY_NONE;

    @Property(type = PropertyType.TEXT, translate = TranslateText.TEXT_3, text = "")
    private String text3Setting = "";

    public AutoTextMod() {
        super(TranslateText.AUTO_TEXT, TranslateText.AUTO_TEXT_DESCRIPTION, ModCategory.PLAYER, "messagetexthotkeymacro");
    }

    @EventTarget
    public void onKey(EventKey event) {

        if (event.getKeyCode() == text1KeybindSetting) {
            mc.thePlayer.sendChatMessage(text1Setting);
        }

        if (event.getKeyCode() == text2KeybindSetting) {
            mc.thePlayer.sendChatMessage(text2Setting);
        }

        if (event.getKeyCode() == text3KeybindSetting) {
            mc.thePlayer.sendChatMessage(text3Setting);
        }
    }
}
