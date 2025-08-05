package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

public class Skin3DMod extends Mod {

    private static Skin3DMod instance;

    private final float baseVoxelSize = 1.15F;
    private final float bodyVoxelWidthSize = 1.05F;
    private final float headVoxelSize = 1.18F;

    private final int renderDistanceLOD = 14;

    public Skin3DMod() {
        super(TranslateText.SKIN_3D, TranslateText.SKIN_3D_DESCRIPTION, ModCategory.RENDER);

        instance = this;
    }

    public static Skin3DMod getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (MoBendsMod.getInstance().isToggled()) {
            MoBendsMod.getInstance().setToggled(false);
        }
    }

    public float getBaseVoxelSize() {
        return baseVoxelSize;
    }

    public float getBodyVoxelWidthSize() {
        return bodyVoxelWidthSize;
    }

    public float getHeadVoxelSize() {
        return headVoxelSize;
    }

    public int getRenderDistanceLOD() {
        return renderDistanceLOD;
    }
}
