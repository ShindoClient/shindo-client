package me.miki.shindo.management.addons.rpo;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.AddonType;
import me.miki.shindo.management.addons.rpo.repository.ResourcePackRepositoryCustom;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RPOAddon extends Addon {

    @Getter
    private static RPOAddon instance;
    private ConfigHandler config;

    public RPOAddon() {
        super("Resource Pack Organizer", "Organizes the resourcepack Screen", "null", AddonType.RENDER);

        instance = this;
    }

    public void init() {
        File configDir = Shindo.getInstance().getFileManager().getAddonConfigDir();
        config = new ConfigHandler(new File(configDir, "rpo.json"));

        List<String> enabled = new ArrayList<>(config.getOptions().getEnabledPacks());

        ResourcePackRepositoryCustom.overrideRepository(enabled);

        Minecraft.getMinecraft().gameSettings.resourcePacks.clear();
        Minecraft.getMinecraft().gameSettings.resourcePacks.addAll(enabled);
        Minecraft.getMinecraft().gameSettings.saveOptions();
        Minecraft.getMinecraft().refreshResources();
    }

    public ConfigHandler get() {
        if (config == null) {
            File configDir = Shindo.getInstance().getFileManager().getAddonConfigDir();
            config = new ConfigHandler(new File(configDir, "rpo.json"));
        }
        return config;
    }


}
