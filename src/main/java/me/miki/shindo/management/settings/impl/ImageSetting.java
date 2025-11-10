package me.miki.shindo.management.settings.impl;

import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;

import java.io.File;

public class ImageSetting extends Setting {

    private File image;

    public ImageSetting(TranslateText nameTranslate, ConfigOwner parent) {
        super(nameTranslate, parent);
        this.image = null;
    }

    public ImageSetting(String name, ConfigOwner parent) {
        super(name, parent);
        this.image = null;
    }

    @Override
    public void reset() {
        this.image = null;
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }
}
