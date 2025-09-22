package me.miki.shindo.management.addons.rpo;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.AddonType;
import me.miki.shindo.management.addons.rpo.repository.ResourcePackRepositoryCustom;
import net.minecraft.client.Minecraft;

import java.io.File;
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
        config = new ConfigHandler(new File(Shindo.getInstance().getFileManager().getAddonsDir(), "rpo.json"));
        List<String> enabled = config.options.getEnabledPacks();

        ResourcePackRepositoryCustom.overrideRepository(enabled);

        Minecraft.getMinecraft().gameSettings.resourcePacks.clear();
        Minecraft.getMinecraft().gameSettings.resourcePacks.addAll(enabled);
        Minecraft.getMinecraft().gameSettings.saveOptions();
        Minecraft.getMinecraft().refreshResources();
    }

    public ConfigHandler get() {
        return config;
    }


}
