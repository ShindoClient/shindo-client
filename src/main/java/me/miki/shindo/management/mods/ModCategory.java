package me.miki.shindo.management.mods;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

public enum ModCategory {
    ALL(TranslateText.ALL),
    PLAYER(TranslateText.PLAYER),
    RENDER(TranslateText.RENDER),
    HUD(TranslateText.HUD),
    WORLD(TranslateText.WORLD),
    OTHER(TranslateText.OTHER);

    private final TranslateText nameTranslate;

    @Getter
    private final ColorAnimation textColorAnimation;

    @Getter
    private final SimpleAnimation backgroundAnimation;

    ModCategory(TranslateText nameTranslate) {
        this.nameTranslate = nameTranslate;
        this.backgroundAnimation = new SimpleAnimation();
        this.textColorAnimation = new ColorAnimation();
    }

    public String getName() {
        return nameTranslate.getText();
    }

}
