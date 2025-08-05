package me.miki.shindo.management.cosmetic.wing.impl;

import me.miki.shindo.management.cosmetic.wing.WingCategory;
import me.miki.shindo.management.roles.ClientRole;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.util.ResourceLocation;

public class Wing {
    private final String name;
    private final ResourceLocation wing;
    private final WingCategory category;
    private final ClientRole requiredRole;

    private final SimpleAnimation animation = new SimpleAnimation();

    public Wing(String name, ResourceLocation wing, WingCategory category, ClientRole requiredRole) {
        this.name = name;
        this.category = category;
        this.wing = wing;
        this.requiredRole = requiredRole;
    }

    public String getName() {
        return name;
    }

    public WingCategory getCategory() {
        return category;
    }

    public SimpleAnimation getAnimation() {
        return animation;
    }

    public ResourceLocation getWing() {
        return wing;
    }

    public ClientRole getRequiredRole() {
        return requiredRole;
    }
}