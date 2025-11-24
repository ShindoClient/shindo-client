package me.miki.shindo.management.skin;

import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.util.ResourceLocation;

public class Skin {

    private final SimpleAnimation animation = new SimpleAnimation();
    private final String id;
    private final String fileName;
    private String profileUuid;
    private String name;
    private ResourceLocation texture;
    private SkinType type;
    private boolean favorite;

    public Skin(String id, String name, String fileName, SkinType type, boolean favorite, ResourceLocation texture, String profileUuid) {
        this.id = id;
        this.name = name;
        this.fileName = fileName;
        this.type = type;
        this.favorite = favorite;
        this.texture = texture;
        this.profileUuid = profileUuid;
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public SimpleAnimation getAnimation() {
        return animation;
    }

    public String getProfileUuid() {
        return profileUuid;
    }

    public void setProfileUuid(String profileUuid) {
        this.profileUuid = profileUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skin skin = (Skin) o;
        return id.equals(skin.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
