package com.shindoclient.shindo.injection.mixin.minecraft.entity;

import com.shindoclient.shindo.management.mods.impl.DamageTiltMod;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public class MixinEntity {

    @Shadow
    public boolean onGround;


    /**
     * Entity position X
     */
    @Shadow
    public double posX;
    /**
     * Entity position Y
     */
    @Shadow
    public double posY;
    /**
     * Entity position Z
     */
    @Shadow
    public double posZ;

    @Inject(method = "spawnRunningParticles", at = @At("HEAD"), cancellable = true)
    private void checkGroundState(CallbackInfo ci) {
        if (!this.onGround) ci.cancel();
    }

    @Redirect(method = "getBrightnessForRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isBlockLoaded(Lnet/minecraft/util/BlockPos;)Z"))
    public boolean alwaysReturnTrue(World world, BlockPos pos) {
        return true;
    }

    @Inject(method = "setVelocity", at = @At("HEAD"))
    public void preSetVelocity(double x, double y, double z, CallbackInfo ci) {
        if (DamageTiltMod.instance.isToggled()) {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (this.equals(player)) {

                float result = (float) (Math.atan2(player.motionZ - z, player.motionX - x) * (180D / Math.PI) - (double) player.rotationYaw);

                if (Float.isFinite(result)) {
                    player.attackedAtYaw = result;
                }
            }
        }
    }

    /**
     * @author MikiDevAHM
     * @reason Custom squared distance calculation for performance optimization
     */
    @Overwrite
    public double getDistanceSq(double x, double y, double z) {
        double dx = this.posX - x;
        double dy = this.posY - y;
        double dz = this.posZ - z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * @author MikiDevAHM
     * @reason Custom distance-to-entity calculation for performance optimization
     */
    @Overwrite
    public float getDistanceToEntity(Entity entityIn) {
        float f = (float) (this.posX - entityIn.posX);
        float f1 = (float) (this.posY - entityIn.posY);
        float f2 = (float) (this.posZ - entityIn.posZ);
        return f * f + f1 * f1 + f2 * f2;
    }
}

