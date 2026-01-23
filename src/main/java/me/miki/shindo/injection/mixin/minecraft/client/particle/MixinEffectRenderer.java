package me.miki.shindo.injection.mixin.minecraft.client.particle;

import net.minecraft.client.particle.EffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectRenderer.class)
public class MixinEffectRenderer {
    @Inject(
            method = {
                    "addBlockDestroyEffects",
                    "addBlockHitEffects(Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;)V"
            }, at = @At("HEAD"), cancellable = true
    )
    private void removeBlockBreakingParticles(CallbackInfo ci) {
        // PatcherAddon removed - block break particles always enabled
    }

    @ModifyConstant(method = "addEffect", constant = @Constant(intValue = 4000))
    private int changeMaxParticleLimit(int original) {
        // PatcherAddon removed - using default particle limit
        return original;
    }

}

