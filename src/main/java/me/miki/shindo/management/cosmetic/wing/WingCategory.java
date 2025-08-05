package me.miki.shindo.management.cosmetic.wing;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

public enum WingCategory {
    ALL(TranslateText.ALL.getText()), LUNAR("Lunar");

    private final String name;
    private final SimpleAnimation backgroundAnimation = new SimpleAnimation();
    private final ColorAnimation textColorAnimation = new ColorAnimation();

    WingCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public SimpleAnimation getBackgroundAnimation() {
        return backgroundAnimation;
    }

    public ColorAnimation getTextColorAnimation() {
        return textColorAnimation;
    }
}