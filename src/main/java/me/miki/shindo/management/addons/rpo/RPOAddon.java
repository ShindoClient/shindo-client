package me.miki.shindo.management.addons.rpo;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.AddonType;
import me.miki.shindo.management.addons.rpo.repository.ResourcePackRepositoryCustom;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RPOAddon extends Addon {

    private static RPOAddon instance;
    private ConfigHandler config;

    public RPOAddon() {
        super("Resource Pack Organizer", "Organizes the resourcepack Screen", LegacyIcon.ADDON_RPO, AddonType.RENDER);

        instance = this;
    }

    public static RPOAddon getInstance() {
        return instance;
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

