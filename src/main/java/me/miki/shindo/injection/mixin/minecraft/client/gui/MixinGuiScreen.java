package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.ui.frame.FrameManager;
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

/**
 * Mixin para GuiScreen que adiciona comportamentos utilitários (efeitos de clique
 * e correções de input), sem integrar o sistema MinecraftUIFramework.
 */
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

    /**
     * Renderiza efeitos de clique após a tela ser desenhada.
     */
    @Inject(method = "drawScreen", at = @At("TAIL"))
    public void postDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        // Renderiza frames do FrameManager (exceto para telas que usam ScreenAnimation,
        // que devem renderizar os frames manualmente após o ScreenAnimation.wrap())
        // Verificamos se a tela atual é GuiGameMenu para evitar renderização duplicada
        String screenClass = this.getClass().getName();
        if (!screenClass.contains("GuiGameMenu")) {
            FrameManager.drawFrames(mouseX, mouseY, partialTicks);
        }
        
        if (InternalSettingsMod.instance.getClickEffectsSetting().isToggled()) {
            Shindo.getInstance().getClickEffects().drawClickEffects();
        }
    }

    /**
     * Intercepta mouseClicked para adicionar efeitos de clique e som.
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void preMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        // Processa cliques nos frames primeiro
        if (FrameManager.hasActiveFrames()) {
            FrameManager.handleMouseClicked(mouseX, mouseY, mouseButton);
            // Cancela o clique na tela base se há frames ativos (frames têm prioridade)
            ci.cancel();
            return;
        }
        
        if (InternalSettingsMod.instance.getClickEffectsSetting().isToggled()) {
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
            // Processa teclas nos frames primeiro
            if (FrameManager.hasActiveFrames()) {
                FrameManager.handleKeyTyped(c, Keyboard.getEventKey());
            } else {
                this.keyTyped(c, Keyboard.getEventKey());
            }
        }

        mc.dispatchKeypresses();
    }
    
    /**
     * Intercepta mouseReleased para processar nos frames.
     */
    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    public void preMouseReleased(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        // Processa soltura do mouse nos frames primeiro
        if (FrameManager.hasActiveFrames()) {
            FrameManager.handleMouseReleased(mouseX, mouseY, mouseButton);
            // Cancela o evento na tela base se há frames ativos
            ci.cancel();
        }
    }

    @Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V"), cancellable = true)
    private void patcher$checkScreen(CallbackInfo ci) {
        if ((Object) this != this.mc.currentScreen) {
            ResolutionHelper.setScaleOverride(-1);
            ci.cancel();
        }
    }
}
