package me.miki.shindo.injection.mixin.mixins.client.renderer.entity;

import me.miki.shindo.management.addons.patcher.PatcherAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandRenderer.class)
public class MixinArmorStandRenderer {
    @Inject(method = "canRenderName(Lnet/minecraft/entity/item/EntityArmorStand;)Z", at = @At("HEAD"), cancellable = true)
    private void patcher$hideNametag(CallbackInfoReturnable<Boolean> cir) {
        if (PatcherAddon.getInstance().isToggled() && PatcherAddon.getInstance().getBetterHideGuiSetting().isToggled() && Minecraft.getMinecraft().gameSettings.hideGUI) {
            cir.setReturnValue(false);
        }
    }
}
