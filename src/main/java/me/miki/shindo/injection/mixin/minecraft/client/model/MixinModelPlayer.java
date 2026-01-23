package me.miki.shindo.injection.mixin.minecraft.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ModelPlayer.class)
public class MixinModelPlayer extends ModelBiped {

    //private ModelRenderer boobs;

    @Shadow
    private boolean smallArms;

    @ModifyConstant(method = "<init>", constant = @Constant(floatValue = 2.5F))
    private float fixAlexArmHeight(float original) {
        // PatcherAddon removed - using original value
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

