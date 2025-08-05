package me.miki.shindo.management.music;

import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.file.FileUtils;

import java.io.File;


public class MusicData {

    private final SimpleAnimation favoriteAnimation = new SimpleAnimation();

    private final String name;
    private final File audio;
    private final File icon;
    private MusicType type;

    public MusicData(File audio, File icon, MusicType type) {
        this.name = FileUtils.getBaseName(audio);
        this.audio = audio;
        this.icon = icon;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public File getAudio() {
        return audio;
    }

    public File getIcon() {
        return icon;
    }

    public MusicType getType() {
        return type;
    }

    public void setType(MusicType type) {
        this.type = type;
    }

    public SimpleAnimation getFavoriteAnimation() {
        return favoriteAnimation;
    }
}