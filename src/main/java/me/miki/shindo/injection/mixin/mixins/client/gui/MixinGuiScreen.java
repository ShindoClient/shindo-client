package me.miki.shindo.injection.mixin.mixins.client.gui;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.utils.Sound;
import me.miki.shindo.utils.helper.ResolutionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    protected Minecraft mc;

    @Shadow
    protected abstract void keyTyped(char typedChar, int keyCode);

    @Inject(method = "drawScreen", at = @At("TAIL"))
    public void postDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (InternalSettingsMod.getInstance().getClickEffectsSetting().isToggled()) {
            Shindo.getInstance().getClickEffects().drawClickEffects();
        }
    }


    @Inject(method = "mouseClicked", at = @At("HEAD"))
    public void preMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (InternalSettingsMod.getInstance().getClickEffectsSetting().isToggled()) {
            Shindo.getInstance().getClickEffects().addClickEffect(mouseX, mouseY);
        }
        Sound.play("shindo/audio/click.wav", true);
    }

    /**
     * @author EldoDebug
     * @reason Handle Keyboard Input
     */
    @Overwrite
    public void handleKeyboardInput() throws IOException {
        char c = Keyboard.getEventCharacter();

        if ((Keyboard.getEventKey() == 0 && c >= ' ') || Keyboard.getEventKeyState()) {
            this.keyTyped(c, Keyboard.getEventKey());
        }

        mc.dispatchKeypresses();
    }

    @Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V"), cancellable = true)
    private void patcher$checkScreen(CallbackInfo ci) {
        if ((Object) this != this.mc.currentScreen) {
            ResolutionHelper.setScaleOverride(-1);
            ci.cancel();
        }
    }
}
