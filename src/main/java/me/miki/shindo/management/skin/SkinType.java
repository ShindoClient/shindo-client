package me.miki.shindo.management.skin;

import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

public enum SkinType {

    DEFAULT(0, "Default"),
    SLIM(1, "Slim");

    private final ColorAnimation textColorAnimation;
    private final SimpleAnimation backgroundAnimation;

    private final String name;
    private final int id;

    SkinType(int id, String name) {
        this.id = id;
        this.name = name;

        this.backgroundAnimation = new SimpleAnimation();
        this.textColorAnimation = new ColorAnimation();
    }

    public static SkinType getTypeById(int id) {

        for (SkinType type : SkinType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return SkinType.DEFAULT;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ColorAnimation getTextColorAnimation() {
        return textColorAnimation;
    }

    public SimpleAnimation getBackgroundAnimation() {
        return backgroundAnimation;
    }
}
