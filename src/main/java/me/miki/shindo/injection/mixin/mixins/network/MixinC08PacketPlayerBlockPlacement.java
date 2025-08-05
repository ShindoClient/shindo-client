package me.miki.shindo.injection.mixin.mixins.network;

import me.miki.shindo.management.mods.impl.ViaVersionMod;
import me.miki.shindo.viaversion.ViaLoadingBase;
import me.miki.shindo.viaversion.protocolinfo.ProtocolInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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
        return ViaVersionMod.getInstance().isLoaded() &&
                ViaVersionMod.getInstance().isToggled() &&
                ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolInfo.R1_11.getProtocolVersion());
    }

}
