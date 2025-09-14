package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.event.impl.EventRenderExpBar;
import me.miki.shindo.management.event.impl.EventRenderTooltip;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.HUDMod;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.ComboSetting;
import me.miki.shindo.management.mods.settings.impl.combo.Option;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ModernHotbarMod extends HUDMod {

    private final SimpleAnimation animation = new SimpleAnimation(0.0F);
    private final ComboSetting designSetting = new ComboSetting(TranslateText.DESIGN, this, TranslateText.CLIENT, new ArrayList<Option>(Arrays.asList(
            new Option(TranslateText.NORMAL), new Option(TranslateText.SHINDO), new Option(TranslateText.CHILL), new Option(TranslateText.CLIENT))));
    private final BooleanSetting smoothSetting = new BooleanSetting(TranslateText.SMOOTH, this, true);
    private final ComboSetting pickupAnimation = new ComboSetting(TranslateText.PICKUP_ANIM, this, TranslateText.PICKUP_POP, new ArrayList<Option>(Arrays.asList(
            new Option(TranslateText.PICKUP_POP), new Option(TranslateText.PICKUP_BREAD), new Option(TranslateText.PICKUP_VANILLA))));
    private float barX, barY, barWidth, barHeight, selX;

    public ModernHotbarMod() {
        super(TranslateText.MODERN_HOTBAR, TranslateText.MODERN_HOTBAR_DESCRIPTION);

        this.setDraggable(false);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        ScaledResolution sr = new ScaledResolution(mc);
        Option option = designSetting.getOption();
        if (this.isEditing()) {
            return;
        }

        nvg.setupAndDraw(() -> drawNanoVG(nvg));

        if (mc.getRenderViewEntity() instanceof EntityPlayer) {

            EntityPlayer entityplayer = (EntityPlayer) mc.getRenderViewEntity();

            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();

            for (int j = 0; j < 9; ++j) {
                int k = sr.getScaledWidth() / 2 - 90 + j * 20 + 2;
                int l = sr.getScaledHeight() - 16 - 3;

                if (option.getTranslate().equals(TranslateText.CHILL)) {
                    l = l + 4;
                }

                renderHotBarItem(j, k, l - 4, event.getPartialTicks(), entityplayer);
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
    }

    private void renderHotBarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer entityPlayer) {
        Option option = pickupAnimation.getOption();
        ItemStack itemstack = entityPlayer.inventory.mainInventory[index];
        boolean animTreatment = option.getTranslate().equals(TranslateText.PICKUP_BREAD);

        if (itemstack != null) {
            float take = (animTreatment) ? partialTicks / 2 : partialTicks;
            float progress = (float) itemstack.animationsToGo - take;
            if (progress > 0.0F) {
                // from betterhotbarmod
                GlStateManager.pushMatrix();
                GlStateManager.translate(xPos + 8, yPos + 12, 0.0F);
                if (option.getTranslate().equals(TranslateText.PICKUP_BREAD)) {
                    float scaleAmount = 1.0F + progress / 2.5F;
                    GlStateManager.scale(Math.max(1.0F, scaleAmount / (1.0F / (scaleAmount / 2))), scaleAmount, 1.0F);
                } else if (option.getTranslate().equals(TranslateText.PICKUP_POP)) {
                    float scaleAmount = 1.0F + progress / 5.0F;
                    GlStateManager.scale(scaleAmount, scaleAmount, 1.0F);
                } else {
                    float scaleAmount = 1.0F + progress / 5.0F;
                    GlStateManager.scale(1.0F / scaleAmount, (scaleAmount + 1.0F) / 2.0F, 1.0F);
                }

                GlStateManager.translate(-(xPos + 8), -(yPos + 12), 0.0F);
            }

            mc.getRenderItem().renderItemAndEffectIntoGUI(itemstack, xPos, yPos);

            if (progress > 0.0F) {
                GlStateManager.popMatrix();
            }

            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, itemstack, xPos, yPos);
        }
    }

    private void drawNanoVG(NanoVGManager nvg) {

        ScaledResolution sr = new ScaledResolution(mc);
        Option option = designSetting.getOption();
        AccentColor currentColor = Shindo.getInstance().getColorManager().getCurrentColor();
        boolean isText = InternalSettingsMod.getInstance().getModThemeSetting().getOption().getTranslate().equals(TranslateText.TEXT);

        if (mc.getRenderViewEntity() instanceof EntityPlayer) {

            if (!option.getTranslate().equals(TranslateText.CHILL)) {

                barX = sr.getScaledWidth() / 2.0F - 91;
                barY = sr.getScaledHeight() - 26;
                barWidth = 91 * 2;
                barHeight = 22;

                if (option.getTranslate().equals(TranslateText.SHINDO)) {
                    nvg.drawShadow(barX, barY, barWidth, barHeight, 6);
                    nvg.drawGradientRoundedRect(barX, barY, barWidth, barHeight, 6, ColorUtils.applyAlpha(currentColor.getColor1(), 190), ColorUtils.applyAlpha(currentColor.getColor2(), 190));
                } else if (option.getTranslate().equals(TranslateText.CLIENT)) {
                    if (isText) {
                        nvg.drawShadow(barX, barY, barWidth, barHeight, 6);
                    }
                    this.setScale(1f);
                    this.setX((int) barX);
                    this.setY((int) barY);
                    drawBackground(barWidth, barHeight, 6);
                } else {
                    nvg.drawShadow(barX, barY, barWidth, barHeight, 6);
                    nvg.drawRoundedRect(barX, barY, barWidth, barHeight, 6, new Color(0, 0, 0, 100));
                }
            } else {

                barX = 0;
                barY = sr.getScaledHeight() - 22;
                barWidth = sr.getScaledWidth();
                barHeight = 22;

                nvg.drawShadow(barX, barY, barWidth, barHeight, 0);
                nvg.drawRect(barX, barY, barWidth, barHeight, new Color(20, 20, 20, 180));
            }

            EntityPlayer entityplayer = (EntityPlayer) mc.getRenderViewEntity();

            int i = sr.getScaledWidth() / 2;

            if (smoothSetting.isToggled()) {
                animation.setAnimation(i - 91 - 1 + entityplayer.inventory.currentItem * 20, 18);
                selX = animation.getValue();
            } else {
                selX = i - 91 - 1 + entityplayer.inventory.currentItem * 20;
            }

            if (!option.getTranslate().equals(TranslateText.CHILL)) {
                if (option.getTranslate().equals(TranslateText.SHINDO)) {
                    nvg.drawRoundedRect(selX + 1, sr.getScaledHeight() - 22 - 4, 22, 22, 6, new Color(255, 255, 255, 140));
                } else {
                    nvg.drawRoundedRect(selX + 1, sr.getScaledHeight() - 22 - 4, 22, 22, 6, new Color(0, 0, 0, 100));
                }
            } else {
                nvg.drawRect(selX + 1, sr.getScaledHeight() - 22, 22, 22, new Color(230, 230, 230, 180));
            }
        }
    }

    @EventTarget
    public void onRenderTooltip(EventRenderTooltip event) {
        event.setCancelled(true);
    }

    @EventTarget
    public void onRenderExpBar(EventRenderExpBar event) {

        Option option = designSetting.getOption();

        event.setCancelled(!option.getTranslate().equals(TranslateText.CHILL));
    }
}
