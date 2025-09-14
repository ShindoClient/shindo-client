package me.miki.shindo.management.cosmetic.bandanna;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

@Getter
public enum BandannaCategory {
    ALL(TranslateText.ALL.getText()), LUNAR("Lunar"), SHINDO("Shindo");

    private final String name;
    private final SimpleAnimation backgroundAnimation = new SimpleAnimation();
    private final ColorAnimation textColorAnimation = new ColorAnimation();

    BandannaCategory(String name) {
        this.name = name;
    }

}
