package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.ui.minecraft.MinecraftUIFramework;
import me.miki.shindo.ui.minecraft.screen.MinecraftScreenRegistry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para interceptar a renderização do menu de configurações de vídeo
 * e aplicar o estilo visual do Shindo Client.
 */
@Mixin(GuiVideoSettings.class)
public class MixinGuiVideoSettings extends GuiScreen {
    
    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiVideoSettings videoSettings = (GuiVideoSettings) (Object) this;
        
        if (MinecraftUIFramework.shouldApplyStyle(videoSettings)) {
            // Renderiza fundo e layout com estilo do Shindo Client
            MinecraftScreenRegistry.renderScreen(videoSettings, mouseX, mouseY, partialTicks);
        }
    }
}
