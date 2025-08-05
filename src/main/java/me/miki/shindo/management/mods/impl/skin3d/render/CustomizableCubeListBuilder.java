package me.miki.shindo.management.mods.impl.skin3d.render;

import com.google.common.collect.Lists;
import me.miki.shindo.management.mods.impl.skin3d.SkinDirection;

import java.util.List;

public class CustomizableCubeListBuilder {

    private final List<CustomizableCube> cubes = Lists.newArrayList();
    private int xTexOffs;
    private int yTexOffs;
    private boolean mirror;

    public static CustomizableCubeListBuilder create() {
        return new CustomizableCubeListBuilder();
    }

    public CustomizableCubeListBuilder texOffs(int i, int j) {
        this.xTexOffs = i;
        this.yTexOffs = j;
        return this;
    }

    public CustomizableCubeListBuilder mirror(boolean bl) {
        this.mirror = bl;
        return this;
    }

    public List<CustomizableCube> getCubes() {
        return cubes;
    }

    public CustomizableCubeListBuilder addBox(float x, float y, float z, float pixelSize, SkinDirection[] hide) {

        int textureSize = 64;
        this.cubes.add(new CustomizableCube(xTexOffs, yTexOffs, x, y, z, pixelSize, pixelSize, pixelSize, 0, 0, 0, this.mirror, textureSize, textureSize, hide));

        return this;
    }

}