package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.ui.minecraft.MinecraftUIFramework;
import me.miki.shindo.ui.minecraft.screen.MinecraftScreenRegistry;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiOptions.class)
public class MixinGuiOptions extends GuiScreen {

    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiOptions options = (GuiOptions) (Object) this;
        
        if (MinecraftUIFramework.shouldApplyStyle(options)) {
            // Renderiza fundo e layout com estilo do Shindo Client
            MinecraftScreenRegistry.renderScreen(options, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void onGuiClosed() {
        mc.gameSettings.saveOptions();
    }
}

