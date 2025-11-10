package me.miki.shindo.management.settings.impl;

import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;

public class TextSetting extends Setting {

    private final String defaultText;
    private String text;

    public TextSetting(TranslateText tText, ConfigOwner parent, String text) {
        super(tText, parent);
        this.text = text;
        this.defaultText = text;
    }

    public TextSetting(String name, ConfigOwner parent, String text) {
        super(name, parent);
        this.text = text;
        this.defaultText = text;
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
