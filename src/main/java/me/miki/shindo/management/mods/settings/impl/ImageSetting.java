package me.miki.shindo.management.mods.settings.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.settings.Setting;

import java.io.File;

public class ImageSetting extends Setting {

    private File image;

    public ImageSetting(TranslateText nameTranslate, Mod parent) {
        super(nameTranslate, parent);

        this.image = null;

        Shindo.getInstance().getModManager().addSettings(this);
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
