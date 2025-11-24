package me.miki.shindo.injection.mixin.mixins.client.gui;

import me.miki.shindo.management.addons.patcher.PatcherAddon;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreenBook.class)
public abstract class MixinGuiScreenBook extends GuiScreen {

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreenBook;handleComponentHover(Lnet/minecraft/util/IChatComponent;II)V"))
    private void callSuper(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void shindo$drawBookBackground(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        PatcherAddon addon = PatcherAddon.getInstance();
        if (addon != null && addon.isToggled() && addon.getBookBackgroundSetting().isToggled()) {
            this.drawWorldBackground(1);
        }
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreenBook;handleComponentHover(Lnet/minecraft/util/IChatComponent;II)V", shift = At.Shift.AFTER), cancellable = true)
    private void cancelFurtherRendering(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        ci.cancel();
    }
}
