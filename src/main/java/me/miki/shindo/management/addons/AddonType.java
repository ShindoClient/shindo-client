package me.miki.shindo.management.addons;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

public enum AddonType {
    ALL(TranslateText.ALL),
    RENDER(TranslateText.RENDER),
    QOL(TranslateText.QOL),
    OTHER(TranslateText.OTHER);


    private final TranslateText nameTranslate;

    private final ColorAnimation textColorAnimation;

    private final SimpleAnimation backgroundAnimation;

    AddonType(TranslateText nameTranslate) {
        this.nameTranslate = nameTranslate;
        this.textColorAnimation = new ColorAnimation();
        this.backgroundAnimation = new SimpleAnimation();
    }

    public String getName() {
        return nameTranslate.getText();
    }

    public ColorAnimation getTextColorAnimation() {
        return textColorAnimation;
    }

    public SimpleAnimation getBackgroundAnimation() {
        return backgroundAnimation;
    }
}
