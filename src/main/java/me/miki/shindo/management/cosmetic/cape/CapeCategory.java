package me.miki.shindo.management.cosmetic.cape;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

public enum CapeCategory {
    ALL(TranslateText.ALL.getText()), MINECON("Minecon"), FLAG("Flags"), /* SOAR("Soar"),*/ CARTOON("Cartoon"), /*MISC("Misc"),*/ CUSTOM("Custom");

    private final String name;
    private final SimpleAnimation backgroundAnimation = new SimpleAnimation();
    private final ColorAnimation textColorAnimation = new ColorAnimation();

    CapeCategory(String name) {
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
