package me.miki.shindo.injection.mixin.mixins.block;

import me.miki.shindo.hooks.CropUtilities;
import me.miki.shindo.management.addons.patcher.PatcherAddon;
import me.miki.shindo.utils.ServerUtils;
import net.minecraft.block.BlockCrops;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(BlockCrops.class)
public class MixinBlockCrops extends MixinBlock{

    //#if MC==10809
    @Override
    public void getSelectedBoundingBox(World worldIn, BlockPos pos, CallbackInfoReturnable<AxisAlignedBB> cir) {
        if (PatcherAddon.getInstance().isToggled() && PatcherAddon.getInstance().getFutureHitboxesSetting().isToggled()  && (ServerUtils.isHypixel() || Minecraft.getMinecraft().isIntegratedServerRunning())) {
            CropUtilities.updateCropsMaxY(worldIn, pos, worldIn.getBlockState(pos).getBlock());
        }
    }

    @Override
    public void collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end, CallbackInfoReturnable<MovingObjectPosition> cir) {
        if (PatcherAddon.getInstance().isToggled() && PatcherAddon.getInstance().getFutureHitboxesSetting().isToggled()  && (ServerUtils.isHypixel() || Minecraft.getMinecraft().isIntegratedServerRunning())) {
            CropUtilities.updateCropsMaxY(worldIn, pos, worldIn.getBlockState(pos).getBlock());
        }
    }
    //#endif
}