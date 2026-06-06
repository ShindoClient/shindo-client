package com.shindoclient.shindo.injection.mixin.minecraft.block;

import com.shindoclient.shindo.management.mods.impl.ClearGlassMod;
import com.shindoclient.shindo.management.settings.impl.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;

@Mixin(BlockStainedGlass.class)
public class MixinBlockStainedGlass extends Block {

    protected MixinBlockStainedGlass(Material materialIn) {
        super(materialIn);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {

        ClearGlassMod clearGlass = ClearGlassMod.instance;
        BooleanSetting stainedSetting = Objects.requireNonNull(clearGlass).getStainedSetting();

        return (!clearGlass.isToggled() || (clearGlass.isToggled() && (stainedSetting == null || !stainedSetting.isToggled()))
                && super.shouldSideBeRendered(worldIn, pos, side));
    }
}

