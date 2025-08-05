package me.miki.shindo.management.profile.mainmenu.impl;

import me.miki.shindo.utils.animation.simple.SimpleAnimation;

public class Background {

    private final SimpleAnimation focusAnimation = new SimpleAnimation();
    private final int id;
    private final String name;

    public Background(int id, String name) {
        this.name = name;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public SimpleAnimation getFocusAnimation() {
        return focusAnimation;
    }

    public String getName() {
        return name == null ? "null" : name;
    }
}
