package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.ui.minecraft.MinecraftUIFramework;
import me.miki.shindo.ui.minecraft.component.MinecraftComponentRegistry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para interceptar a renderização de listas de servidores
 * e aplicar o estilo visual do Shindo Client.
 */
@Mixin(ServerSelectionList.class)
public class MixinServerSelectionList {
    
    @Inject(
        method = "drawScreen",
        at = @At("HEAD")
    )
    private void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        ServerSelectionList list = (ServerSelectionList) (Object) this;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        GuiScreen currentScreen = mc.currentScreen;
        
        if (MinecraftUIFramework.shouldApplyStyle(currentScreen)) {
            MinecraftComponentRegistry.renderList(list, mouseX, mouseY, partialTicks);
        }
    }
}
