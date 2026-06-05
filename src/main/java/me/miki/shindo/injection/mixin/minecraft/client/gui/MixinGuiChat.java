package me.miki.shindo.injection.mixin.minecraft.client.gui;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat extends GuiScreen {

}
