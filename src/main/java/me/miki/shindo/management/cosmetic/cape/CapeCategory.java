package me.miki.shindo.management.cosmetic.cape;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

@Getter
public enum CapeCategory {
    ALL(TranslateText.ALL.getText()), MINECON("Minecon"), FLAG("Flags"), CARTOON("Cartoon"), CUSTOM("Custom");

    private final String name;
    private final SimpleAnimation backgroundAnimation = new SimpleAnimation();
    private final ColorAnimation textColorAnimation = new ColorAnimation();

    CapeCategory(String name) {
        this.name = name;
    }

}
