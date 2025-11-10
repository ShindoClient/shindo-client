package me.miki.shindo.management.settings.impl;

import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;

import java.io.File;

public class SoundSetting extends Setting {

    private File sound;

    public SoundSetting(TranslateText nameTranslate, ConfigOwner parent) {
        super(nameTranslate, parent);
        this.sound = null;
    }

    public SoundSetting(String name, ConfigOwner parent) {
        super(name, parent);
        this.sound = null;
    }

    @Override
    public void reset() {
        this.sound = null;
    }

    public File getSound() {
        return sound;
    }

    public void setSound(File sound) {
        this.sound = sound;
    }
}
