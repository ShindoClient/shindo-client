package me.miki.shindo.management.mods.settings;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;

public class Setting {

    @Getter
    private final Mod parent;
    private final TranslateText nameTranslate;

    public Setting(TranslateText nameTranslate, Mod parent) {
        this.nameTranslate = nameTranslate;
        this.parent = parent;
    }

    public void reset() {
    }

    public String getName() {
        return nameTranslate.getText();
    }

    public String getNameKey() {
        return nameTranslate.getKey();
    }
}
