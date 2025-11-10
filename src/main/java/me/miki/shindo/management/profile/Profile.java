package me.miki.shindo.management.profile;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

import java.io.File;

@Getter
public class Profile {

    private final SimpleAnimation starAnimation = new SimpleAnimation();

    private final File jsonFile;
    private final int id;
    private final String name;
    private final ProfileIcon icon;
    @Setter
    private File customIcon;
    @Setter
    private String serverIp;
    @Setter
    private ProfileType type;

    public Profile(int id, String serverIp, File jsonFile, ProfileIcon icon, File customIcon) {
        this.id = id;
        this.jsonFile = jsonFile;
        this.name = jsonFile != null ? jsonFile.getName().replace(".json", "") : "";
        this.serverIp = serverIp;
        this.icon = icon;
        this.customIcon = customIcon;
        this.type = ProfileType.ALL;
    }

}
