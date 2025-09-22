package me.miki.shindo.injection.mixin.mixins.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.miki.shindo.Shindo;
import me.miki.shindo.hooks.ServerDataHook;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@Mixin(ServerListEntryNormal.class)
public abstract class MixinServerListEntryNormal implements GuiListExtended.IGuiListEntry {


    @Shadow
    @Final
    private static ThreadPoolExecutor field_148302_b = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());
    @Shadow
    @Final
    private static ResourceLocation UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");
    @Shadow
    @Final
    private static ResourceLocation SERVER_SELECTION_BUTTONS = new ResourceLocation("textures/gui/server_selection.png");
    @Shadow
    @Final
    private ServerData server;

    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    @Final
    private GuiMultiplayer owner;
    @Shadow
    @Final
    private ResourceLocation serverIcon;
    @Shadow
    private String field_148299_g;
    @Shadow
    private DynamicTexture field_148305_h;
    @Shadow
    private long field_148298_f;

    @Shadow
    protected abstract void prepareServerIcon();

    @Shadow
    protected abstract void drawTextureAt(int p_178012_1_, int p_178012_2_, ResourceLocation p_178012_3_);

    @Shadow
    protected abstract boolean func_178013_b();

    /**
     * @author MikiDevAHM
     * @reason Featured Servers Star icon + server icon crash fix + remove arrow icon from Featured Servers
     */
    @Overwrite
    @SuppressWarnings("ConstantConditions")
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
        if (!this.server.field_78841_f) {
            this.server.field_78841_f = true;
            this.server.pingToServer = -2L;
            this.server.serverMOTD = "";
            this.server.populationInfo = "";
            field_148302_b.submit(new Runnable() {
                public void run() {
                    try {
                        owner.getOldServerPinger().ping(server);
                    } catch (UnknownHostException var2) {
                        server.pingToServer = -1L;
                        server.serverMOTD = EnumChatFormatting.DARK_RED + "Can't resolve hostname";
                    } catch (Exception var3) {
                        server.pingToServer = -1L;
                        server.serverMOTD = EnumChatFormatting.DARK_RED + "Can't connect to server.";
                    }
                }
            });
        }

        boolean isFeaturedServer = (server instanceof ServerDataHook);

        if (isFeaturedServer) {
            Shindo instance = Shindo.getInstance();
            NanoVGManager nvg = instance.getNanoVGManager();

            nvg.setupAndDraw(() -> nvg.drawText(LegacyIcon.STAR_FILL, x - 20, y + 10, Color.YELLOW, 14F, Fonts.LEGACYICON));
        }

        boolean flag = this.server.version > 47;
        boolean flag1 = this.server.version < 47;
        boolean flag2 = flag || flag1;
        this.mc.fontRendererObj.drawString(this.server.serverName, x + 32 + 3, y + 1, 16777215);
        List<String> list = this.mc.fontRendererObj.listFormattedStringToWidth(this.server.serverMOTD, listWidth - 32 - 2);

        for (int i = 0; i < Math.min(list.size(), 2); ++i) {
            this.mc.fontRendererObj.drawString(list.get(i), x + 32 + 3, y + 12 + this.mc.fontRendererObj.FONT_HEIGHT * i, 8421504);
        }

        String s2 = flag2 ? EnumChatFormatting.DARK_RED + this.server.gameVersion : this.server.populationInfo;
        int j = this.mc.fontRendererObj.getStringWidth(s2);
        this.mc.fontRendererObj.drawString(s2, x + listWidth - j - 15 - 2, y + 1, 8421504);
        int k = 0;
        String s = null;
        int l;
        String s1;

        if (flag2) {
            l = 5;
            s1 = flag ? "Client out of date!" : "Server out of date!";
            s = this.server.playerList;
        } else if (this.server.field_78841_f && this.server.pingToServer != -2L) {
            if (this.server.pingToServer < 0L) {
                l = 5;
            } else if (this.server.pingToServer < 150L) {
                l = 0;
            } else if (this.server.pingToServer < 300L) {
                l = 1;
            } else if (this.server.pingToServer < 600L) {
                l = 2;
            } else if (this.server.pingToServer < 1000L) {
                l = 3;
            } else {
                l = 4;
            }

            if (this.server.pingToServer < 0L) {
                s1 = "(no connection)";
            } else {
                s1 = this.server.pingToServer + "ms";
                s = this.server.playerList;
            }
        } else {
            k = 1;
            l = (int) (Minecraft.getSystemTime() / 100L + (slotIndex * 2L) & 7L);

            if (l > 4) {
                l = 8 - l;
            }

            s1 = "Pinging...";
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Gui.icons);
        Gui.drawModalRectWithCustomSizedTexture(x + listWidth - 15, y, (float) (k * 10), (float) (176 + l * 8), 10, 8, 256.0F, 256.0F);

        if (this.server.getBase64EncodedIconData() != null && !this.server.getBase64EncodedIconData().equals(this.field_148299_g)) {
            this.field_148299_g = this.server.getBase64EncodedIconData();
            try {
                prepareServerIcon();
            } catch (Exception e) {
                ShindoLogger.error("Failed to prepare server icon, setting to default.", e);
                server.setBase64EncodedIconData(null);
            }
            this.owner.getServerList().saveServerList();
        }

        if (this.field_148305_h != null) {
            this.drawTextureAt(x, y, this.serverIcon);
        } else {
            this.drawTextureAt(x, y, UNKNOWN_SERVER);
        }

        int i1 = mouseX - x;
        int j1 = mouseY - y;

        if (i1 >= listWidth - 15 && i1 <= listWidth - 5 && j1 >= 0 && j1 <= 8) {
            this.owner.setHoveringText(s1);
        } else if (i1 >= listWidth - j - 15 - 2 && i1 <= listWidth - 15 - 2 && j1 >= 0 && j1 <= 8) {
            this.owner.setHoveringText(s);
        }

        if (this.mc.gameSettings.touchscreen || isSelected) {
            this.mc.getTextureManager().bindTexture(SERVER_SELECTION_BUTTONS);
            Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int k1 = mouseX - x;
            int l1 = mouseY - y;

            if (this.func_178013_b()) {
                if (k1 < 32 && k1 > 16) {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }

            if (!isFeaturedServer) {
                if (this.owner.func_175392_a((ServerListEntryNormal) (Object) this, slotIndex)) {
                    if (k1 < 16 && l1 < 16) {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                    } else {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                    }
                }

                if (this.owner.func_175394_b((ServerListEntryNormal) (Object) this, slotIndex)) {
                    if (k1 < 16 && l1 > 16) {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                    } else {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                    }
                }
            }
        }
    }

    /**
     * @author MikiDevAHM
     * @reason Remove Arrow Icons From Featured Servers
     */
    @Overwrite
    @SuppressWarnings("ConstantConditions")
    public boolean mousePressed(int slotIndex, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_) {
        if (p_148278_5_ <= 32) {
            if (p_148278_5_ < 32 && p_148278_5_ > 16 && this.func_178013_b()) {
                this.owner.selectServer(slotIndex);
                this.owner.connectToSelected();
                return true;
            }

            if (!(this.owner.getServerList().getServerData(slotIndex) instanceof ServerDataHook)) {
                if (p_148278_5_ < 16 && p_148278_6_ < 16 && this.owner.func_175392_a((ServerListEntryNormal) (Object) this, slotIndex)) {
                    this.owner.func_175391_a((ServerListEntryNormal) (Object) this, slotIndex, GuiScreen.isShiftKeyDown());
                    return true;
                }

                if (p_148278_5_ < 16 && p_148278_6_ > 16 && this.owner.func_175394_b((ServerListEntryNormal) (Object) this, slotIndex)) {
                    this.owner.func_175393_b((ServerListEntryNormal) (Object) this, slotIndex, GuiScreen.isShiftKeyDown());
                    return true;
                }
            }
        }

        this.owner.selectServer(slotIndex);

        if (Minecraft.getSystemTime() - this.field_148298_f < 250L) {
            this.owner.connectToSelected();
        }

        this.field_148298_f = Minecraft.getSystemTime();
        return false;
    }


}
