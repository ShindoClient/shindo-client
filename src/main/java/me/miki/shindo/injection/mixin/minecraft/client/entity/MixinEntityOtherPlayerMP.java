package me.miki.shindo.injection.mixin.minecraft.client.entity;

import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityOtherPlayerMP.class)
public class MixinEntityOtherPlayerMP {

    @Inject(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityOtherPlayerMP;updateArmSwingProgress()V", shift = At.Shift.AFTER), cancellable = true)
    private void removeUselessAnimations(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "setPositionAndRotation2", at = @At("HEAD"))
    private void onSetPositionAndRotation(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_, CallbackInfo ci) {
        if (this instanceof IMixinEntityPlayer) {
            ((IMixinEntityPlayer) this).getPlayerDataSamples().setPositionAndRotation(x, y, z, yaw, pitch);
        }
    }

}

