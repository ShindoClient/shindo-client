package me.miki.shindo.management.screenshot;

import me.miki.shindo.utils.animation.simple.SimpleAnimation;

import java.io.File;

public class Screenshot {

    private final SimpleAnimation selectAnimation = new SimpleAnimation();

    private final String name;
    private final File image;

    public Screenshot(File image) {
        this.image = image;
        this.name = image.getName().replace(".png", "");
    }

    public SimpleAnimation getSelectAnimation() {
        return selectAnimation;
    }

    public String getName() {
        return name;
    }

    public File getImage() {
        return image;
    }
}
