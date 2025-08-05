package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventKey;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.KeybindSetting;
import me.miki.shindo.management.mods.settings.impl.TextSetting;
import org.lwjgl.input.Keyboard;

public class AutoTextMod extends Mod {

    private final KeybindSetting text1KeybindSetting = new KeybindSetting(TranslateText.TEXT_1_KEY, this, Keyboard.KEY_NONE);
    private final TextSetting text1Setting = new TextSetting(TranslateText.TEXT_1, this, "");
    private final KeybindSetting text2KeybindSetting = new KeybindSetting(TranslateText.TEXT_2_KEY, this, Keyboard.KEY_NONE);
    private final TextSetting text2Setting = new TextSetting(TranslateText.TEXT_2, this, "");
    private final KeybindSetting text3KeybindSetting = new KeybindSetting(TranslateText.TEXT_3_KEY, this, Keyboard.KEY_NONE);
    private final TextSetting text3Setting = new TextSetting(TranslateText.TEXT_3, this, "");

    public AutoTextMod() {
        super(TranslateText.AUTO_TEXT, TranslateText.AUTO_TEXT_DESCRIPTION, ModCategory.PLAYER, "messagetexthotkeymacro");
    }

    @EventTarget
    public void onKey(EventKey event) {

        if (event.getKeyCode() == text1KeybindSetting.getKeyCode()) {
            mc.thePlayer.sendChatMessage(text1Setting.getText());
        }

        if (event.getKeyCode() == text2KeybindSetting.getKeyCode()) {
            mc.thePlayer.sendChatMessage(text2Setting.getText());
        }

        if (event.getKeyCode() == text3KeybindSetting.getKeyCode()) {
            mc.thePlayer.sendChatMessage(text3Setting.getText());
        }
    }
}
