package me.miki.shindo.management.cosmetic.bandanna.impl;

import lombok.Getter;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.management.cosmetic.bandanna.BandannaCategory;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.util.ResourceLocation;

@Getter
public class Bandanna {

    private final String name;
    private final ResourceLocation bandanna;
    private final BandannaCategory category;
    private final Role requiredRole;

    private final SimpleAnimation animation = new SimpleAnimation();

    public Bandanna(String name, ResourceLocation bandanna, BandannaCategory category, Role requiredRole) {
        this.name = name;
        this.category = category;
        this.bandanna = bandanna;
        this.requiredRole = requiredRole;
    }

}
