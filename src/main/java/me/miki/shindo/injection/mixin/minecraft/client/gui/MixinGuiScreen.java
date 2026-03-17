package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.sound.Sound;
import me.miki.shindo.management.sound.Sounds;
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
import java.util.Objects;

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

        if (Objects.requireNonNull(InternalSettingsMod.instance.getClickEffectsSetting()).isToggled()) {
            Shindo.getInstance().getClickEffects().drawClickEffects();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void preMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {

        if (Objects.requireNonNull(InternalSettingsMod.instance.getClickEffectsSetting()).isToggled()) {
            Shindo.getInstance().getClickEffects().addClickEffect(mouseX, mouseY);
        }
        Sound.play(Sounds.SHINDO_AUDIO_CLICK, true);
    }

    /**
     * @author MikiDevAHM
     * @reason Improve keyboard input handling to properly forward key events
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
    private void checkScreen(CallbackInfo ci) {
        if ((Object) this != this.mc.currentScreen) {
            ci.cancel();
        }
    }
}



