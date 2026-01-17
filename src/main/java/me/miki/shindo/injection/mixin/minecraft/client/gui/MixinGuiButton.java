package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.ui.minecraft.MinecraftUIFramework;
import me.miki.shindo.ui.minecraft.component.MinecraftComponentRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para interceptar a renderização de botões do Minecraft
 * e aplicar o estilo visual do Shindo Client.
 */
@Mixin(GuiButton.class)
public class MixinGuiButton {
    
    /**
     * Intercepta a renderização do botão e aplica o estilo do Shindo Client.
     */
    @Inject(
        method = "drawButton",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onDrawButton(net.minecraft.client.Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        // Verifica se o framework está habilitado e se deve aplicar o estilo
        GuiButton button = (GuiButton) (Object) this;
        
        // Obtém a tela atual
        GuiScreen currentScreen = mc.currentScreen;
        
        if (MinecraftUIFramework.shouldApplyStyle(currentScreen)) {
            // Renderiza com o estilo do Shindo Client
            MinecraftComponentRegistry.renderButton(button, mouseX, mouseY, mc.getRenderPartialTicks());
            ci.cancel(); // Cancela a renderização padrão do Minecraft
        }
    }
}
