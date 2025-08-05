package me.miki.shindo.management.cosmetic.cape.impl;

import me.miki.shindo.management.cosmetic.cape.CapeCategory;
import me.miki.shindo.management.roles.ClientRole;
import net.minecraft.util.ResourceLocation;

import java.io.File;

public class CustomCape extends Cape {

    private final File sample;

    public CustomCape(String name, File sample, ResourceLocation cape, CapeCategory category, ClientRole requiredRole) {
        super(name, cape, category, requiredRole);
        this.sample = sample;
    }

    public File getSample() {
        return sample;
    }
}
