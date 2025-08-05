package me.miki.shindo.management.mods.settings.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.settings.Setting;

public class TextSetting extends Setting {

    private final String defaultText;
    private String text;

    public TextSetting(TranslateText tText, Mod parent, String text) {
        super(tText, parent);
        this.text = text;
        this.defaultText = text;

        Shindo.getInstance().getModManager().addSettings(this);
    }

    @Override
    public void reset() {
        this.text = defaultText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDefaultText() {
        return defaultText;
    }
}
