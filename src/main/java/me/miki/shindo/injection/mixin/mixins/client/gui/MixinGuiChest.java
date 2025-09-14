package me.miki.shindo.injection.mixin.mixins.client.gui;

import me.miki.shindo.hooks.ContainerOpacityHook;
import net.minecraft.client.gui.inventory.GuiChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChest.class)
public class MixinGuiChest {

    @Inject(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/inventory/GuiChest;drawTexturedModalRect(IIIIII)V", ordinal = 0))
    private void beginContainerOpacity(CallbackInfo ci) {
        ContainerOpacityHook.beginTransparency();
    }

    @Inject(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/inventory/GuiChest;drawTexturedModalRect(IIIIII)V", ordinal = 1, shift = At.Shift.AFTER))
    private void endContainerOpacity(CallbackInfo ci) {
        ContainerOpacityHook.endTransparency();
    }

}
