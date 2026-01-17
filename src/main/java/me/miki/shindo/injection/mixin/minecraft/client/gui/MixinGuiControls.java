package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.ui.minecraft.MinecraftUIFramework;
import me.miki.shindo.ui.minecraft.screen.MinecraftScreenRegistry;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para interceptar a renderização do menu de controles
 * e aplicar o estilo visual do Shindo Client.
 */
@Mixin(GuiControls.class)
public class MixinGuiControls extends GuiScreen {
    
    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiControls controls = (GuiControls) (Object) this;
        
        if (MinecraftUIFramework.shouldApplyStyle(controls)) {
            // Renderiza fundo e layout com estilo do Shindo Client
            MinecraftScreenRegistry.renderScreen(controls, mouseX, mouseY, partialTicks);
        }
    }
}
