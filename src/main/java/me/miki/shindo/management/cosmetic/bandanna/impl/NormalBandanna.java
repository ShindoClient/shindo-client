package me.miki.shindo.management.cosmetic.bandanna.impl;

import lombok.Getter;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.management.cosmetic.bandanna.BandannaCategory;
import net.minecraft.util.ResourceLocation;

@Getter
public class NormalBandanna extends Bandanna {
    private final ResourceLocation sample;

    public NormalBandanna(String name, ResourceLocation sample, ResourceLocation bandanna, BandannaCategory category, Role requiredRole) {
        super(name, bandanna, category, requiredRole);
        this.sample = sample;
    }
}
