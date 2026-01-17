package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.ui.minecraft.MinecraftUIFramework;
import me.miki.shindo.ui.minecraft.component.MinecraftComponentRegistry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para interceptar a renderização de campos de texto do Minecraft
 * e aplicar o estilo visual do Shindo Client.
 */
@Mixin(GuiTextField.class)
public class MixinGuiTextField {
    
    /**
     * Intercepta a renderização do campo de texto e aplica o estilo do Shindo Client.
     */
    @Inject(
        method = "drawTextBox",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onDrawTextBox(CallbackInfo ci) {
        GuiTextField textField = (GuiTextField) (Object) this;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        
        // Obtém a tela atual
        GuiScreen currentScreen = mc.currentScreen;
        
        if (currentScreen != null && MinecraftUIFramework.shouldApplyStyle(currentScreen)) {
            // Obtém posição do mouse
            int mouseX = org.lwjgl.input.Mouse.getX() * currentScreen.width / mc.displayWidth;
            int mouseY = currentScreen.height - org.lwjgl.input.Mouse.getY() * currentScreen.height / mc.displayHeight - 1;
            
            // Renderiza com o estilo do Shindo Client
            MinecraftComponentRegistry.renderTextField(textField, mouseX, mouseY, mc.getRenderPartialTicks());
            ci.cancel(); // Cancela a renderização padrão do Minecraft
        }
    }
}
