package me.miki.shindo.management.mods.settings.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.settings.Setting;

import java.io.File;

public class SoundSetting extends Setting {

    private File sound;

    public SoundSetting(TranslateText nameTranslate, Mod parent) {
        super(nameTranslate, parent);

        this.sound = null;

        Shindo.getInstance().getModManager().addSettings(this);
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
