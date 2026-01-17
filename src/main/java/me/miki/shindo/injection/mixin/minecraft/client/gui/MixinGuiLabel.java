package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.ui.minecraft.MinecraftUIFramework;
import me.miki.shindo.ui.minecraft.component.MinecraftComponentRegistry;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para interceptar a renderização de labels do Minecraft
 * e aplicar o estilo visual do Shindo Client.
 */
@Mixin(GuiLabel.class)
public class MixinGuiLabel {
    
    /**
     * Intercepta a renderização do label e aplica o estilo do Shindo Client.
     */
    @Inject(
        method = "drawLabel",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onDrawLabel(net.minecraft.client.Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        GuiLabel label = (GuiLabel) (Object) this;
        GuiScreen currentScreen = mc.currentScreen;
        
        if (MinecraftUIFramework.shouldApplyStyle(currentScreen)) {
            // Labels usam FontRenderer nativo, então apenas verificamos se deve aplicar estilo
            // A renderização de texto permanece nativa para manter compatibilidade
            MinecraftComponentRegistry.renderLabel(label, mouseX, mouseY, mc.getRenderPartialTicks());
        }
    }
}
