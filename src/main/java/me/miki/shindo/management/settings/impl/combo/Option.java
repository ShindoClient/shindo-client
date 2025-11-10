package me.miki.shindo.management.settings.impl.combo;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;

public class Option {

    private final TranslateText nameTranslate;
    private final String fallbackName;
    private final String nameKey;

    public Option(TranslateText nameTranslate) {
        this.nameTranslate = nameTranslate;
        this.fallbackName = nameTranslate.getText();
        this.nameKey = nameTranslate.getKey();
    }

    public Option(String name) {
        this.nameTranslate = null;
        this.fallbackName = name;
        this.nameKey = buildKey(name);
    }

    public String getName() {
        return nameTranslate != null ? nameTranslate.getText() : fallbackName;
    }

    public String getNameKey() {
        return nameKey;
    }

    public TranslateText getTranslate() {
        return nameTranslate;
    }

    private static String buildKey(String raw) {
        return raw == null ? "" : Setting.normalizeKey(raw);
    }
}
