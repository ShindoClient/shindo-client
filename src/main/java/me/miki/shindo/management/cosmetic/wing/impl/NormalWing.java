package me.miki.shindo.management.cosmetic.wing.impl;

import me.miki.shindo.management.cosmetic.wing.WingCategory;
import me.miki.shindo.management.roles.ClientRole;
import net.minecraft.util.ResourceLocation;

public class NormalWing extends Wing {

    private final ResourceLocation sample;

    public NormalWing(String name, ResourceLocation sample, ResourceLocation wing, WingCategory category, ClientRole requiredRole) {
        super(name, wing, category, requiredRole);
        this.sample = sample;
    }

    public ResourceLocation getSample() {
        return sample;
    }

}
