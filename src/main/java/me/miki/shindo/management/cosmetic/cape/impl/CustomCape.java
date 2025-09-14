package me.miki.shindo.management.cosmetic.cape.impl;

import lombok.Getter;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.management.cosmetic.cape.CapeCategory;
import net.minecraft.util.ResourceLocation;

import java.io.File;

@Getter
public class CustomCape extends Cape {

    private final File sample;

    public CustomCape(String name, File sample, ResourceLocation cape, CapeCategory category, Role requiredRole) {
        super(name, cape, category, requiredRole);
        this.sample = sample;
    }

}
