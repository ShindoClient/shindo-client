package me.miki.shindo.injection.mixin.mixins.client.gui;

import me.miki.shindo.management.mods.impl.InventoryMod;
import me.miki.shindo.management.mods.impl.InventoryMod.AnimationType;
import me.miki.shindo.ui.particle.ParticleEngine;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.easing.EaseBackIn;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends GuiScreen {

    @Unique
    private final ParticleEngine client$particle = new ParticleEngine();
    @Shadow
    private int dragSplittingButton;

    @Shadow
    private int dragSplittingRemnant;

    @Unique
    private SimpleAnimation client$xAnimation;
    @Unique
    private SimpleAnimation client$yAnimation;

    @Unique
    private Animation client$xAnimationBackIn;
    @Unique
    private Animation client$yAnimationBackIn;

    @Shadow
    protected abstract boolean checkHotbarKeys(int keyCode);

    @Inject(method = "initGui", at = @At("RETURN"))
    public void initGui(CallbackInfo ci) {

        InventoryMod mod = InventoryMod.getInstance();

        if (mod.isToggled() && mod.getAnimationSetting().isToggled()) {

            AnimationType type = mod.getAnimationType();

            if (type == AnimationType.NORMAL) {
                client$xAnimation = new SimpleAnimation(0.0F);
                client$yAnimation = new SimpleAnimation(0.0F);
            } else {
                client$xAnimationBackIn = new EaseBackIn(380, this.width, 2);
                client$yAnimationBackIn = new EaseBackIn(380, this.height, 2);
            }
        }
    }

    @Inject(method = "drawScreen", at = @At("HEAD"))
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {

        InventoryMod mod = InventoryMod.getInstance();

        if (!mod.isToggled() || (mod.isToggled() && mod.getBackgroundSetting().isToggled())) {
            this.drawDefaultBackground();
        }

        if (mod.isToggled() && mod.getParticleSetting().isToggled()) {
            client$particle.draw(mouseX, mouseY);
        }

        if (mod.isToggled() && mod.getAnimationSetting().isToggled()) {

            double xmod = 0;
            double ymod = 0;

            AnimationType type = mod.getAnimationType();

            if (type == AnimationType.NORMAL) {

                client$xAnimation.setAnimation(this.width, 18);
                client$yAnimation.setAnimation(this.height, 18);

                xmod = this.width / 2F - (client$xAnimation.getValue() / 2);
                ymod = this.height / 2F - (client$yAnimation.getValue() / 2);

                GlStateManager.translate(xmod, ymod, 0);
                GlStateManager.scale(client$xAnimation.getValue() / this.width, client$yAnimation.getValue() / this.height, 1);
            } else {
                xmod = this.width / 2F - (client$xAnimationBackIn.getValue() / 2);
                ymod = this.height / 2F - (client$yAnimationBackIn.getValue() / 2);

                GlStateManager.translate(xmod, ymod, 0);
                GlStateManager.scale(client$xAnimationBackIn.getValue() / this.width, client$yAnimationBackIn.getValue() / this.height, 1);
            }
        }
    }

    @Inject(method = "updateDragSplitting", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void fixRemnants(CallbackInfo ci) {
        if (this.dragSplittingButton == 2) {
            this.dragSplittingRemnant = mc.thePlayer.inventory.getItemStack().getMaxStackSize();
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void preMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (mouseButton - 100 == mc.gameSettings.keyBindInventory.getKeyCode()) {
            mc.thePlayer.closeScreen();
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void postMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        checkHotbarKeys(mouseButton - 100);
    }

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawDefaultBackground()V"))
    public void removeDrawDefaultBackground(GuiContainer instance) {
    }
}
