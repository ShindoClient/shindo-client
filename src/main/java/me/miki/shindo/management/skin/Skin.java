package me.miki.shindo.management.skin;

import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.util.ResourceLocation;

public class Skin {

    private final SimpleAnimation animation = new SimpleAnimation();
    private String name;
    private ResourceLocation texture;
    private SkinType type;

    public Skin(String name, ResourceLocation texture, SkinType type) {
        this.name = name;
        this.texture = texture;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String setName(String name) {
        return this.name = name;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public SkinType getType() {
        return type;
    }

    public void setType(SkinType type) {
        this.type = type;
    }

    public SimpleAnimation getAnimation() {
        return animation;
    }
}