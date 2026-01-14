package me.miki.shindo.injection.mixin.minecraft.block;

import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.impl.ClearGlassMod;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockGlass.class)
public class MixinBlockGlass extends Block {

    protected MixinBlockGlass(Material materialIn) {
        super(materialIn);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {

        ClearGlassMod clearGlass = ClearGlassMod.instance;
        BooleanSetting normalSetting = clearGlass.getNormalSetting();

        return (!clearGlass.isToggled() || (clearGlass.isToggled() && (normalSetting == null || !normalSetting.isToggled()))
                && super.shouldSideBeRendered(worldIn, pos, side));
    }
}

