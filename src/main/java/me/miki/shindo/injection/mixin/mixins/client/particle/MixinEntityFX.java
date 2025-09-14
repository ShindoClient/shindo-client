package me.miki.shindo.injection.mixin.mixins.client.particle;

import me.miki.shindo.injection.interfaces.IMixinEntityFX;
import me.miki.shindo.management.addons.patcher.PatcherAddon;
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
    public void setCullState(float cullState) {
        this.cullState = cullState;
    }

    @Override
    public float getCullState() {
        return this.cullState;
    }

    @Redirect(method = "renderParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/EntityFX;getBrightnessForRender(F)I"))
    private int staticParticleColor(EntityFX entityFX, float partialTicks) {
        return PatcherAddon.getInstance().isToggled() && PatcherAddon.getInstance().getStaticParticleColorSetting().isToggled() ? 15728880 : entityFX.getBrightnessForRender(partialTicks);
    }
}
