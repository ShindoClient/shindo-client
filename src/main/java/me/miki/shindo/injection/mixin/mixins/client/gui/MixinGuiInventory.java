package me.miki.shindo.injection.mixin.mixins.client.gui;

import me.miki.shindo.hooks.ContainerOpacityHook;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiInventory.class)
public class MixinGuiInventory {
    @Inject(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/inventory/GuiInventory;drawTexturedModalRect(IIIIII)V"))
    private void beginContainerOpacity(CallbackInfo ci) {
        ContainerOpacityHook.beginTransparency();
    }

    @Inject(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/inventory/GuiInventory;drawTexturedModalRect(IIIIII)V", shift = At.Shift.AFTER))
    private void endContainerOpacity(CallbackInfo ci) {
        ContainerOpacityHook.endTransparency();
    }
}
