package me.miki.shindo.injection.mixin.minecraft.client.model;

import me.miki.shindo.management.addons.patcher.PatcherAddon;
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


}

