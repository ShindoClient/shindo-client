package me.miki.shindo.management.profile;

import me.miki.shindo.utils.animation.simple.SimpleAnimation;

import java.io.File;

public class Profile {

    private final SimpleAnimation starAnimation = new SimpleAnimation();

    private final File jsonFile;
    private final int id;
    private final String name;
    private final ProfileIcon icon;
    private String serverIp;
    private ProfileType type;

    public Profile(int id, String serverIp, File jsonFile, ProfileIcon icon) {
        this.id = id;
        this.jsonFile = jsonFile;
        this.name = jsonFile != null ? jsonFile.getName().replace(".json", "") : "";
        this.serverIp = serverIp;
        this.icon = icon;
        this.type = ProfileType.ALL;
    }

    public int getId() {
        return id;
    }

    public File getJsonFile() {
        return jsonFile;
    }

    public ProfileIcon getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public ProfileType getType() {
        return type;
    }

    public void setType(ProfileType type) {
        this.type = type;
    }

    public SimpleAnimation getStarAnimation() {
        return starAnimation;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
}
