package me.miki.shindo.injection.mixin.minecraft.network;

import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon;
import me.miki.shindo.management.mods.impl.ViaVersionMod;
import me.miki.shindo.viaversion.ViaLoadingBase;
import me.miki.shindo.viaversion.protocolinfo.ProtocolInfo;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(C08PacketPlayerBlockPlacement.class)
public class MixinC08PacketPlayerBlockPlacement {

    @Shadow
    private BlockPos position;

    @Shadow
    private int placedBlockDirection;

    @Shadow
    private ItemStack stack;

    @Shadow
    private float facingX;

    @Shadow
    private float facingY;

    @Shadow
    private float facingZ;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void readPacketData(PacketBuffer buf) throws IOException {

        float amount = isNewVersion() ? 1 : 16.0F;

        this.position = buf.readBlockPos();
        this.placedBlockDirection = buf.readUnsignedByte();
        this.stack = buf.readItemStackFromBuffer();
        this.facingX = (float) buf.readUnsignedByte() / amount;
        this.facingY = (float) buf.readUnsignedByte() / amount;
        this.facingZ = (float) buf.readUnsignedByte() / amount;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void writePacketData(PacketBuffer buf) throws IOException {

        float amount = isNewVersion() ? 1 : 16.0F;

        buf.writeBlockPos(this.position);
        buf.writeByte(this.placedBlockDirection);
        buf.writeItemStackToBuffer(this.stack);
        buf.writeByte((int) (this.facingX * amount));
        buf.writeByte((int) (this.facingY * amount));
        buf.writeByte((int) (this.facingZ * amount));
    }

    private boolean isNewVersion() {
        ViaVersionMod viaMod = ViaVersionMod.instance;
        return viaMod.isLoaded() &&
                viaMod.isToggled() &&
                ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolInfo.R1_11.getProtocolVersion());
    }
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        // HackerDetector: Rastreia colocação de blocos através de pacotes
        if (stack != null && stack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) stack.getItem()).getBlock();
            HackerDetectorAddon.getInstance().onPlayerBlockPacket(position, placedBlockDirection, block);
        }
    }

}

