package me.miki.shindo.management.cosmetic.cape.impl;

import lombok.Getter;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.management.cosmetic.cape.CapeCategory;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.util.ResourceLocation;

@Getter
public class Cape {

    private final String name;
    private final ResourceLocation cape;
    private final CapeCategory category;
    private final Role requiredRole;

    private final SimpleAnimation animation = new SimpleAnimation();

    public Cape(String name, ResourceLocation cape, CapeCategory category, Role requiredRole) {
        this.name = name;
        this.category = category;
        this.cape = cape;
        this.requiredRole = requiredRole;
    }

}
