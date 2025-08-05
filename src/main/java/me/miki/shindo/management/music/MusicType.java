package me.miki.shindo.management.music;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

public enum MusicType {
    ALL(0, TranslateText.ALL), FAVORITE(1, TranslateText.FAVORITE);

    private final ColorAnimation textColorAnimation;
    private final SimpleAnimation backgroundAnimation;

    private final TranslateText nameTranslate;
    private final int id;

    MusicType(int id, TranslateText nameTranslate) {
        this.id = id;
        this.nameTranslate = nameTranslate;
        this.backgroundAnimation = new SimpleAnimation();
        this.textColorAnimation = new ColorAnimation();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return nameTranslate.getText();
    }

    public String getKey() {
        return nameTranslate.getKey();
    }

    public ColorAnimation getTextColorAnimation() {
        return textColorAnimation;
    }

    public SimpleAnimation getBackgroundAnimation() {
        return backgroundAnimation;
    }
}
