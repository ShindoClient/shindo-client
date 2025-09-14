package me.miki.shindo.management.cosmetic.cape.impl;

import lombok.Getter;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.management.cosmetic.cape.CapeCategory;
import net.minecraft.util.ResourceLocation;

@Getter
public class NormalCape extends Cape {

    private final ResourceLocation sample;

    public NormalCape(String name, ResourceLocation sample, ResourceLocation cape, CapeCategory category, Role requiredRole) {
        super(name, cape, category, requiredRole);
        this.sample = sample;
    }

}
