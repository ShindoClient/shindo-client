package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

@Getter
public class Skin3DMod extends Mod {

    @Getter
    private static Skin3DMod instance;

    private final float baseVoxelSize = 1.15F;
    private final float bodyVoxelWidthSize = 1.05F;
    private final float headVoxelSize = 1.18F;

    private final int renderDistanceLOD = 14;

    public Skin3DMod() {
        super(TranslateText.SKIN_3D, TranslateText.SKIN_3D_DESCRIPTION, ModCategory.RENDER);

        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (MoBendsMod.getInstance().isToggled()) {
            MoBendsMod.getInstance().setToggled(false);
        }
    }

}
