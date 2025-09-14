package me.miki.shindo.management.cosmetic.wing;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

@Getter
public enum WingCategory {
    ALL(TranslateText.ALL.getText()), SHINDO("Shindo"), LUNAR("Lunar");

    private final String name;
    private final SimpleAnimation backgroundAnimation = new SimpleAnimation();
    private final ColorAnimation textColorAnimation = new ColorAnimation();

    WingCategory(String name) {
        this.name = name;
    }

}