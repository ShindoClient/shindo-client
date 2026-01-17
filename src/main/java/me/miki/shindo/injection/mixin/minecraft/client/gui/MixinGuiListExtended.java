package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.ui.minecraft.MinecraftUIFramework;
import me.miki.shindo.ui.minecraft.component.MinecraftComponentRegistry;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para interceptar a renderização de listas do Minecraft (GuiListExtended)
 * e aplicar o estilo visual do Shindo Client.
 */
@Mixin(GuiListExtended.class)
public class MixinGuiListExtended {
    
    /**
     * Intercepta a renderização da lista e aplica o estilo do Shindo Client.
     */
    @Inject(
        method = "drawScreen",
        at = @At("HEAD")
    )
    private void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiListExtended list = (GuiListExtended) (Object) this;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        GuiScreen currentScreen = mc.currentScreen;
        
        if (MinecraftUIFramework.shouldApplyStyle(currentScreen)) {
            // Renderiza estilo customizado antes da renderização padrão
            MinecraftComponentRegistry.renderList(list, mouseX, mouseY, partialTicks);
        }
    }
}
