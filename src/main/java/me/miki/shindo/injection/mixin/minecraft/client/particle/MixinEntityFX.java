package me.miki.shindo.injection.mixin.minecraft.client.particle;

import me.miki.shindo.injection.mixin.interfaces.client.particle.IMixinEntityFX;
import net.minecraft.client.particle.EntityFX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityFX.class)
public class MixinEntityFX implements IMixinEntityFX {

    @Unique
    private float cullState;

    @Override
    public float getCullState() {
        return this.cullState;
    }

    @Override
    public void setCullState(float cullState) {
        this.cullState = cullState;
    }

    @Redirect(method = "renderParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/EntityFX;getBrightnessForRender(F)I"))
    private int staticParticleColor(EntityFX entityFX, float partialTicks) {
        // PatcherAddon removed - using default brightness
        return entityFX.getBrightnessForRender(partialTicks);
    }
}


