package me.miki.shindo.management.profile;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

public enum ProfileType {
    ALL(0, TranslateText.ALL), FAVORITE(1, TranslateText.FAVORITE);

    private final int id;
    private final TranslateText nameTranslate;
    private final ColorAnimation textColorAnimation;
    private final SimpleAnimation backgroundAnimation;

    ProfileType(int id, TranslateText nameTranslate) {
        this.id = id;
        this.nameTranslate = nameTranslate;
        this.backgroundAnimation = new SimpleAnimation();
        this.textColorAnimation = new ColorAnimation();
    }

    public static ProfileType getTypeById(int id) {

        for (ProfileType type : ProfileType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return ProfileType.ALL;
    }

    public String getName() {
        return nameTranslate.getText();
    }

    public String getKey() {
        return nameTranslate.getKey();
    }

    public SimpleAnimation getBackgroundAnimation() {
        return backgroundAnimation;
    }

    public ColorAnimation getTextColorAnimation() {
        return textColorAnimation;
    }

    public int getId() {
        return id;
    }
}
