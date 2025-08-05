package me.miki.shindo.injection.mixin.mixins.gui;

import me.miki.shindo.Shindo;
import me.miki.shindo.ShindoAPI;
import me.miki.shindo.management.mods.impl.TabEditorMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.roles.ClientRole;
import me.miki.shindo.management.roles.ClientRoleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;
import java.util.Collection;
import java.util.UUID;

@Mixin(GuiPlayerTabOverlay.class)
public abstract class MixinGuiPlayerTabOverlay extends Gui {


    @Shadow
    public abstract String getPlayerName(NetworkPlayerInfo info);

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", ordinal = 2))
    public int renderShindoIcon(FontRenderer fontRenderer, String text, float x, float y, int color) {
        Minecraft mc = Minecraft.getMinecraft();
        Collection<NetworkPlayerInfo> players = mc.getNetHandler().getPlayerInfoMap();

        UUID uuid = null;
        for (NetworkPlayerInfo info : players) {
            String name = this.getPlayerName(info); // mesmo método usado na TabList
            if (name.equals(text)) {
                uuid = info.getGameProfile().getId();
                break;
            }
        }

        if (uuid != null) {
            ShindoAPI api = Shindo.getInstance().getShindoAPI();
            NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

            if (api.isOnline(uuid)) {
                Color iconColor;

                if (ClientRoleManager.hasRole(uuid, ClientRole.STAFF)) iconColor = new Color(178, 2, 2);
                else if (ClientRoleManager.hasRole(uuid, ClientRole.DIAMOND)) iconColor = new Color(2, 194, 172);
                else if (ClientRoleManager.hasRole(uuid, ClientRole.GOLD)) iconColor = new Color(227, 216, 0);
                else iconColor = Color.WHITE;

                float iconX = x; // desenha à esquerda do texto
                float iconY = y;

                nvg.setupAndDraw(() -> {
                    nvg.drawText(LegacyIcon.SHINDO, iconX, iconY, iconColor, 8F, Fonts.LEGACYICON);
                });
                x += 10;
            }
        }

        return fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getPlayerEntityByUUID(Ljava/util/UUID;)Lnet/minecraft/entity/player/EntityPlayer;"))
    public EntityPlayer removePlayerHead(WorldClient instance, UUID uuid) {

        if (TabEditorMod.getInstance().isToggled() && !TabEditorMod.getInstance().getHeadSetting().isToggled()) {
            return null;
        }

        return instance.getPlayerEntityByUUID(uuid);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isIntegratedServerRunning()Z"))
    public boolean removePlayerHead(Minecraft instance) {
        return instance.isIntegratedServerRunning() && showHeads();
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;getIsencrypted()Z"))
    public boolean removePlayerHead(NetworkManager instance) {
        return instance.getIsencrypted() && showHeads();
    }

    /**
     * @author MikiDevAHM
     * @reason Rendering Shindo Logo and Other Stuff
     */
    @Overwrite
    protected void drawPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn) {
        final int ping = networkPlayerInfoIn.getResponseTime();
        final int x = p_175245_2_ + p_175245_1_ - (Minecraft.getMinecraft().fontRendererObj.getStringWidth(ping + "") >> 1) - 2;
        final int y = p_175245_3_ + (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT >> 2);
        if (TabEditorMod.getInstance().isToggled() && TabEditorMod.getInstance().getPingSetting().isToggled()) {
            int colour;

            if (ping > 500) {
                colour = 11141120;
            } else if (ping > 300) {
                colour = 11184640;
            } else if (ping > 200) {
                colour = 11193344;
            } else if (ping > 135) {
                colour = 2128640;
            } else if (ping > 70) {
                colour = 39168;
            } else if (ping >= 0) {
                colour = 47872;
            } else {
                colour = 11141120;
            }

            if (ping >= 0 && ping < 10000) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5f, 0.5f, 0.5f);
                Minecraft.getMinecraft().fontRendererObj.drawString("   " + ping, (2 * x) - 10, (2 * y), colour);
                GlStateManager.scale(2.0f, 2.0f, 2.0f);
                GlStateManager.popMatrix();
            }

            return;
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.icons);
        int i = 0;
        int j;

        if (ping < 0) {
            j = 5;
        } else if (ping < 150) {
            j = 0;
        } else if (ping < 300) {
            j = 1;
        } else if (ping < 600) {
            j = 2;
        } else if (ping < 1000) {
            j = 3;
        } else {
            j = 4;
        }

        this.zLevel += 100.0F;
        this.drawTexturedModalRect(p_175245_2_ + p_175245_1_ - 11, p_175245_3_, i * 10, 176 + j * 8, 10, 8);
        this.zLevel -= 100.0F;
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = Integer.MIN_VALUE))
    public int removeBackground(int original) {

        if (TabEditorMod.getInstance().isToggled() && !TabEditorMod.getInstance().getBackgroundSetting().isToggled()) {
            return new Color(0, 0, 0, 0).getRGB();
        }

        return original;
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 553648127))
    public int removeBackground2(int original) {

        if (TabEditorMod.getInstance().isToggled() && !TabEditorMod.getInstance().getBackgroundSetting().isToggled()) {
            return new Color(0, 0, 0, 0).getRGB();
        }

        return original;
    }

    private boolean showHeads() {
        return !(TabEditorMod.getInstance().isToggled() && !TabEditorMod.getInstance().getHeadSetting().isToggled());
    }

}