package me.miki.shindo.injection.mixin.minecraft.client.renderer.entity.layer;

import me.miki.shindo.management.mods.impl.AnimationsMod;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LayerArmorBase.class)
public class MixinLayerArmorBase {

    @Inject(method = "shouldCombineTextures", at = @At("HEAD"), cancellable = true)
    public void oldArmorDamage(CallbackInfoReturnable<Boolean> cir) {

        AnimationsMod mod = AnimationsMod.instance;
        BooleanSetting armorDamageSetting = mod.getArmorDamageSetting();

        cir.setReturnValue(mod.isToggled() && armorDamageSetting != null && armorDamageSetting.isToggled());
    }

    @Inject(method = "renderGlint", at = @At("HEAD"), cancellable = true)
    private void disableEnchantGlint(CallbackInfo ci) {
        // PatcherAddon removed - enchant glint always enabled
    }
}

