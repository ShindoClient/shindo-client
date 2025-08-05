package me.miki.shindo.management.cosmetic.cape.impl;

import me.miki.shindo.management.cosmetic.cape.CapeCategory;
import me.miki.shindo.management.roles.ClientRole;
import net.minecraft.util.ResourceLocation;

public class NormalCape extends Cape {

    private final ResourceLocation sample;

    public NormalCape(String name, ResourceLocation sample, ResourceLocation cape, CapeCategory category, ClientRole requiredRole) {
        super(name, cape, category, requiredRole);
        this.sample = sample;
    }

    public ResourceLocation getSample() {
        return sample;
    }
}
