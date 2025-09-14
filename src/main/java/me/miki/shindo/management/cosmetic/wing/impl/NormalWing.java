package me.miki.shindo.management.cosmetic.wing.impl;

import lombok.Getter;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.management.cosmetic.wing.WingCategory;
import net.minecraft.util.ResourceLocation;

@Getter
public class NormalWing extends Wing {

    private final ResourceLocation sample;

    public NormalWing(String name, ResourceLocation sample, ResourceLocation wing, WingCategory category, Role requiredRole) {
        super(name, wing, category, requiredRole);
        this.sample = sample;
    }
}
