package me.miki.shindo.management.profile.mainmenu.impl;

import me.miki.shindo.utils.animation.simple.SimpleAnimation;

import java.io.File;

public class CustomBackground extends Background {

    private final SimpleAnimation trashAnimation = new SimpleAnimation();
    private final File image;

    public CustomBackground(int id, String name, File image) {
        super(id, name);
        this.image = image;
    }

    public File getImage() {
        return image;
    }

    public SimpleAnimation getTrashAnimation() {
        return trashAnimation;
    }
}
