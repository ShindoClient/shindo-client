package me.miki.shindo.management.mods.settings.impl.combo;

import me.miki.shindo.management.language.TranslateText;

public class Option {

    private final TranslateText nameTranslate;

    public Option(TranslateText nameTranslate) {
        this.nameTranslate = nameTranslate;
    }

    public String getName() {
        return nameTranslate.getText();
    }

    public String getNameKey() {
        return nameTranslate.getKey();
    }

    public TranslateText getTranslate() {
        return nameTranslate;
    }
}
