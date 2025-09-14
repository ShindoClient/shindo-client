package me.miki.shindo.injection.mixin.mixins.client.model;

import me.miki.shindo.management.addons.patcher.PatcherAddon;
import me.miki.shindo.management.mods.impl.WaveyCapesMod;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPlayer.class)
public class MixinModelPlayer extends ModelBiped {

    //private ModelRenderer boobs;

    @Shadow
    private boolean smallArms;

    @ModifyConstant(method = "<init>", constant = @Constant(floatValue = 2.5F))
    private float fixAlexArmHeight(float original) {
        PatcherAddon addon = PatcherAddon.getInstance();
        if (addon != null) {
            if (addon.isToggled() && addon.getFixedAlexArmsSetting().isToggled()) return 2.0F;
        }

        return original;
    }

    /**
     * @author asbyth
     * @reason Resolve item positions being incorrect on Alex models (MC-72397)
     */
    @Overwrite
    public void postRenderArm(float scale) {
        if (this.smallArms) {
            this.bipedRightArm.rotationPointX += 0.5F;
            this.bipedRightArm.postRender(scale);
            this.bipedRightArm.rotationPointZ -= 0.5F;
        } else {
            this.bipedRightArm.postRender(scale);
        }
    }


    @Inject(method = "renderCape", at = @At("HEAD"), cancellable = true)
    public void renderCloak(float p_renderCape_1_, CallbackInfo ci) {
        if (WaveyCapesMod.getInstance().isToggled()) {
            ci.cancel();
        }
    }

    //@Inject(method = "<init>", at = @At("RETURN"))
    //private void boobs(float size, boolean z, CallbackInfo c) {
    //    boobs = new ModelRenderer(this, 16, 20);
    //    boobs.addBox(-4F, -1.5F, -5F, 8, 4, 4, size);
    //}

    //@Inject(method = "render", at = @At("RETURN"))
    //public void render(Entity e, float v, float w, float x, float y, float z, float scale, CallbackInfo c) {
    //    GlStateManager.pushMatrix();
    //    boobs.showModel = FemaleGenderMod.getInstance().isToggled() && e == Minecraft.getMinecraft().thePlayer;
    //    boobs.render(scale);
    //    // move to pos when sneaking
    //    boobs.offsetY = e.isSneaking() ? .25F : 0F;
    //    boobs.offsetZ = e.isSneaking() ? .1F : 0F;
    //    // rotate so they face up
    //    boobs.rotateAngleX = 45;
    //    GlStateManager.popMatrix();
    //}
}
