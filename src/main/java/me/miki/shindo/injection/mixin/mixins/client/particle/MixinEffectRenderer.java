package me.miki.shindo.injection.mixin.mixins.client.particle;

import me.miki.shindo.management.addons.patcher.PatcherAddon;
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
        if (PatcherAddon.getInstance().isToggled() && PatcherAddon.getInstance().getDisableBlockBreakParticlesSetting().isToggled()) {
            ci.cancel();
        }
    }

    @ModifyConstant(method = "addEffect", constant = @Constant(intValue = 4000))
    private int changeMaxParticleLimit(int original) {
        return PatcherAddon.getInstance().isToggled() ? PatcherAddon.getInstance().getMaxParticleLimitSetting().getValueInt() : original;
    }

}
