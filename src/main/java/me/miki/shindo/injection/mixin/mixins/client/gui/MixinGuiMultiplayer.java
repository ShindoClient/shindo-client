package me.miki.shindo.injection.mixin.mixins.client.gui;

import me.miki.shindo.gui.GuiFixConnecting;
import me.miki.shindo.hooks.ServerDataHook;
import me.miki.shindo.injection.interfaces.IMixinServerList;
import me.miki.shindo.management.mods.impl.ViaVersionMod;
import me.miki.shindo.viaversion.ViaShindo;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMultiplayer.class)
public class MixinGuiMultiplayer extends GuiScreen {

    @Shadow
    private ServerSelectionList serverListSelector;

    @Shadow
    private GuiButton btnEditServer;

    @Shadow
    private GuiButton btnSelectServer;

    @Shadow
    private GuiButton btnDeleteServer;

    @Shadow private ServerList savedServerList;

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

    /**
     * @author MikiDevAHM
     * @reason Featured Servers Protection
     */
    @Overwrite
    public void selectServer(int index) {
        this.serverListSelector.setSelectedSlotIndex(index);
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = index < 0 ? null : this.serverListSelector.getListEntry(index);
        this.btnSelectServer.enabled = false;
        this.btnEditServer.enabled = false;
        this.btnDeleteServer.enabled = false;

        if (guilistextended$iguilistentry != null && !(guilistextended$iguilistentry instanceof ServerListEntryLanScan))
        {
            this.btnSelectServer.enabled = true;

            if (guilistextended$iguilistentry instanceof ServerListEntryNormal)
            {
                this.btnEditServer.enabled = true;
                this.btnDeleteServer.enabled = true;
            }

            // MODIFIED CODE
            if (savedServerList.getServerData(index) instanceof ServerDataHook) {
                this.btnEditServer.enabled = false;
                this.btnDeleteServer.enabled = false;
            }
        }
    }

    /**
     * @author MikiDevAHM
     * @reason
     */
    @Overwrite
    public boolean func_175392_a(ServerListEntryNormal p_175392_1_, int p_175392_2_)
    {
        return p_175392_2_ > ((IMixinServerList) savedServerList).getFeaturedServerCount();
    }
}
