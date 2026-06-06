package com.shindoclient.shindo.injection.mixin.minecraft.client.gui;

import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiLanguage.class)
public class MixinGuiLanguage extends GuiScreen {

    @Override
    public void onGuiClosed() {
        mc.ingameGUI.getChatGUI().refreshChat();
    }
}