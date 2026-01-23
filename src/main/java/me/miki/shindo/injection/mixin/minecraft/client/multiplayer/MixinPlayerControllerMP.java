package me.miki.shindo.injection.mixin.minecraft.client.multiplayer;

import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon;
import me.miki.shindo.management.mods.impl.AnimationsMod;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Final
    @Shadow
    private final Minecraft mc = Minecraft.getMinecraft();

    @Mutable
    @Final
    @Shadow
    private final NetHandlerPlayClient netClientHandler;

    @Shadow
    private BlockPos currentBlock = new BlockPos(-1, -1, -1);

    @Shadow
    private boolean isHittingBlock;

    @Shadow
    private float curBlockDamageMP;

    protected MixinPlayerControllerMP(NetHandlerPlayClient netClientHandler) {
        this.netClientHandler = netClientHandler;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void resetBlockRemoving() {

        if (isHittingBlock) {
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, currentBlock, EnumFacing.DOWN));
        }

        isHittingBlock = false;
        curBlockDamageMP = 0.0F;
        mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), currentBlock, -1);
    }

    @Inject(method = "getIsHittingBlock", at = @At("HEAD"), cancellable = true)
    private void cancelHit(CallbackInfoReturnable<Boolean> cir) {

        AnimationsMod mod = AnimationsMod.instance;
        BooleanSetting pushingSetting = mod.getPushingSetting();
        BooleanSetting blockHitSetting = mod.getBlockHitSetting();

        if (mod.isToggled()
                && pushingSetting != null && pushingSetting.isToggled()
                && blockHitSetting != null && blockHitSetting.isToggled()) {
            cir.setReturnValue(false);
        }
    }
    
    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"))
    private void onPlayerDestroyBlock(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        // HackerDetector: Rastreia quebra de blocos
        if (mc.theWorld != null && mc.thePlayer != null) {
            IBlockState state = mc.theWorld.getBlockState(pos);
            Block block = state.getBlock();
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            String tool = heldItem != null ? heldItem.getItem().getUnlocalizedName() : "hand";
            HackerDetectorAddon.getInstance().addBrokenBlock(block, pos, tool);
        }
    }
    
    @Inject(method = "onPlayerRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;onItemUse(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;FFF)Z", shift = At.Shift.AFTER))
    private void onPlayerRightClick(EntityPlayerSP player, WorldClient worldIn, ItemStack heldStack, BlockPos pos, EnumFacing side, Vec3 hitVec, CallbackInfoReturnable<Boolean> cir) {

        // HackerDetector: Rastreia colocação de blocos
        if (pos != null && worldIn != null && !worldIn.isAirBlock(pos)) {
            IBlockState state = worldIn.getBlockState(pos);
            HackerDetectorAddon.getInstance().addPlacedBlock(pos, state);
        }
    }
}

