package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.ui.minecraft.MinecraftUIFramework;
import me.miki.shindo.ui.minecraft.component.MinecraftComponentRegistry;
import net.minecraft.client.gui.GuiResourcePackAvailable;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para interceptar a renderização de listas de resource packs disponíveis
 * e aplicar o estilo visual do Shindo Client.
 */
@Mixin(GuiResourcePackAvailable.class)
public class MixinGuiResourcePackAvailable {
    
    @Inject(
        method = "drawScreen",
        at = @At("HEAD")
    )
    private void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiResourcePackAvailable list = (GuiResourcePackAvailable) (Object) this;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        GuiScreen currentScreen = mc.currentScreen;
        
        if (MinecraftUIFramework.shouldApplyStyle(currentScreen)) {
            MinecraftComponentRegistry.renderList(list, mouseX, mouseY, partialTicks);
        }
    }
}
