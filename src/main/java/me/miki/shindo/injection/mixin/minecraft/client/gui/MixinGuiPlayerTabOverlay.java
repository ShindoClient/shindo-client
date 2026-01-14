package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.Shindo;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.api.roles.RoleVisuals;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.impl.TabEditorMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.settings.impl.BooleanSetting;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(GuiPlayerTabOverlay.class)
public abstract class MixinGuiPlayerTabOverlay extends Gui {

    @Unique
    private final Map<String, UUID> shindo$tabNameCache = new HashMap<>();

    @Shadow
    public abstract String getPlayerName(NetworkPlayerInfo info);

    @Inject(method = "renderPlayerlist", at = @At("HEAD"))
    private void shindo$cacheTabNames(CallbackInfo ci) {
        shindo$tabNameCache.clear();
        Minecraft mc = Minecraft.getMinecraft();
        Collection<NetworkPlayerInfo> players = mc.getNetHandler().getPlayerInfoMap();
        for (NetworkPlayerInfo info : players) {
            String name = this.getPlayerName(info);
            if (name != null && !name.isEmpty()) {
                shindo$tabNameCache.put(name, info.getGameProfile().getId());
            }
        }
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", ordinal = 2))
    public int renderShindoIcon(FontRenderer fontRenderer, String text, float x, float y, int color) {
        UUID uuid = shindo$tabNameCache.get(text);

        if (uuid != null) {
            NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

            if (RoleVisuals.isOnline(uuid)) {
                Role role = RoleVisuals.getPrimaryRoleCached(uuid);
                Color iconColor = RoleVisuals.getRoleColor(role);

                float iconX = x;
                float iconY = y;

                if (nvg != null) {
                    nvg.setupAndDraw(() -> {
                        nvg.drawText(RoleVisuals.getTabIcon(role), iconX, iconY, iconColor, 8F, Fonts.LEGACYICON);
                    });
                    x += 10;
                } else {
                    String fallback = RoleVisuals.getTabFallbackText(role);
                    fontRenderer.drawStringWithShadow(fallback, iconX, iconY, iconColor.getRGB());
                    x += fontRenderer.getStringWidth(fallback) + 2;
                }
            }
        }

        return fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getPlayerEntityByUUID(Ljava/util/UUID;)Lnet/minecraft/entity/player/EntityPlayer;"))
    public EntityPlayer removePlayerHead(WorldClient instance, UUID uuid) {

        TabEditorMod tabMod = TabEditorMod.instance;
        BooleanSetting headSetting = tabMod.getHeadSetting();
        if (tabMod.isToggled() && headSetting != null && !headSetting.isToggled()) {
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

    @Inject(method = "drawPing", at = @At("HEAD"), cancellable = true)
    private void shindo$drawPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn, CallbackInfo ci) {
        final int ping = networkPlayerInfoIn.getResponseTime();
        final int x = p_175245_2_ + p_175245_1_ - (Minecraft.getMinecraft().fontRendererObj.getStringWidth(ping + "") >> 1) - 2;
        final int y = p_175245_3_ + (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT >> 2);
        TabEditorMod tabMod = TabEditorMod.instance;
        BooleanSetting pingSetting = tabMod.getPingSetting();
        if (tabMod.isToggled() && pingSetting != null && pingSetting.isToggled()) {
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

            ci.cancel();
        }
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = Integer.MIN_VALUE))
    public int removeBackground(int original) {

        TabEditorMod tabMod = TabEditorMod.instance;
        BooleanSetting backgroundSetting = tabMod.getBackgroundSetting();
        if (tabMod.isToggled() && backgroundSetting != null && !backgroundSetting.isToggled()) {
            return new Color(0, 0, 0, 0).getRGB();
        }

        return original;
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 553648127))
    public int removeBackground2(int original) {

        TabEditorMod tabMod = TabEditorMod.instance;
        BooleanSetting backgroundSetting = tabMod.getBackgroundSetting();
        if (tabMod.isToggled() && backgroundSetting != null && !backgroundSetting.isToggled()) {
            return new Color(0, 0, 0, 0).getRGB();
        }

        return original;
    }

    private boolean showHeads() {
        TabEditorMod tabMod = TabEditorMod.instance;
        BooleanSetting headSetting = tabMod.getHeadSetting();
        return !(tabMod.isToggled() && headSetting != null && !headSetting.isToggled());
    }

}

