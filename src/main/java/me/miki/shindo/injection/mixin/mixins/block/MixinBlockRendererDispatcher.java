package me.miki.shindo.injection.mixin.mixins.block;

import com.google.common.collect.Sets;
import me.miki.shindo.management.addons.patcher.PatcherAddon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(BlockRendererDispatcher.class)
public class MixinBlockRendererDispatcher {
    @Unique
    private static final Set<Block> patcher$foliageBlocks = Sets.newHashSet(
            Blocks.tallgrass,
            Blocks.double_plant,
            Blocks.red_flower,
            Blocks.yellow_flower
    );

    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    private void patcher$cancelFoliage(IBlockState state, BlockPos pos, IBlockAccess blockAccess, WorldRenderer worldRendererIn, CallbackInfoReturnable<Boolean> cir) {
        if (PatcherAddon.getInstance().isToggled() && PatcherAddon.getInstance().getRemoveGroundFoliageSetting().isToggled() && patcher$foliageBlocks.contains(state.getBlock())) {
            cir.setReturnValue(false);
        }
    }
}
