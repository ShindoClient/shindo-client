package me.miki.shindo.injection.mixin.mixins.gui;

import me.miki.shindo.gui.GuiFixConnecting;
import me.miki.shindo.management.mods.impl.ViaVersionMod;
import me.miki.shindo.viaversion.ViaShindo;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMultiplayer.class)
public class MixinGuiMultiplayer extends GuiScreen {


    @Inject(method = "initGui", at = @At("TAIL"))
    public void preInitGui(CallbackInfo ci) {
        if (ViaVersionMod.getInstance().isToggled()) {
            this.buttonList.add(ViaShindo.getInstance().getAsyncVersionSlider());
        }
    }

    /**
     * @author EldoDebug
     * @reason Add GuiFixConnecting
     */
    @Overwrite
    private void connectToServer(ServerData server) {
        mc.displayGuiScreen(new GuiFixConnecting(this, mc, server));
    }
}
