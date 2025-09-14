package me.miki.shindo.management.cosmetic.wing.impl;

import lombok.Getter;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.management.cosmetic.wing.WingCategory;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.util.ResourceLocation;

@Getter
public class Wing {
    private final String name;
    private final ResourceLocation wing;
    private final WingCategory category;
    private final Role requiredRole;

    private final SimpleAnimation animation = new SimpleAnimation();

    public Wing(String name, ResourceLocation wing, WingCategory category, Role requiredRole) {
        this.name = name;
        this.category = category;
        this.wing = wing;
        this.requiredRole = requiredRole;
    }

}