package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.HUDMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class PlayerDisplayMod extends HUDMod {

    @Property(type = PropertyType.NUMBER, translate = TranslateText.YAW_OFFSET, min = -90, max = 120, current = 0, step = 1)
    private int yawOffsetSetting = 0;

    public PlayerDisplayMod() {
        super(TranslateText.PLAYER_DISPLAY, TranslateText.PLAYER_DISPLAY_DESCRIPTION, "paperdoll");
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        GlStateManager.enableColorMaterial();
        GlStateManager.enableDepth();
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.getX() + (15 * this.getScale()), this.getY() + (58 * this.getScale()), -500.0F);
        GlStateManager.scale(-this.getScale() * 30, this.getScale() * 30, this.getScale() * 30);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(mc.thePlayer.rotationYaw + yawOffsetSetting, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();

        rendermanager.setRenderShadow(false);
        rendermanager.doRenderEntity(mc.thePlayer, 0.0D, 0.0D, 0.0D, 0.0F, event.getPartialTicks(), true);
        rendermanager.setRenderShadow(true);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();

        this.setWidth(30);
        this.setHeight(60);
    }
}
