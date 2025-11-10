package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRenderHitbox;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class HitBoxMod extends Mod {

    private final Color eyeHeightColor = Color.RED;
    private final Color lookVectorColor = Color.BLUE;

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR)
    private Color colorSetting = new Color(255, 255, 255);
    @Property(type = PropertyType.NUMBER, translate = TranslateText.ALPHA, min = 0, max = 1.0, current = 1)
    private double alphaSetting = 1;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BOUNDING_BOX)
    private boolean boundingBoxSetting = true;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.EYE_HEIGHT)
    private boolean eyeHeightSetting = true;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.LOOK_VECTOR)
    private boolean lookVectorSetting = true;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.LINE_WIDTH, min = 1, max = 5, current = 2, step = 1)
    private int lineWidthSetting = 2;

    public HitBoxMod() {
        super(TranslateText.HITBOX, TranslateText.HITBOX_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onRenderHitbox(EventRenderHitbox event) {

        float half = event.getEntity().width / 2.0F;

        event.setCancelled(true);

        if (event.getEntity() instanceof EntityArmorStand) {
            return;
        }

        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GL11.glLineWidth(lineWidthSetting);

        if (boundingBoxSetting) {
            AxisAlignedBB box = event.getEntity().getEntityBoundingBox();
            AxisAlignedBB offsetBox = new AxisAlignedBB(box.minX - event.getEntity().posX + event.getX(),
                    box.minY - event.getEntity().posY + event.getY(), box.minZ - event.getEntity().posZ + event.getZ(),
                    box.maxX - event.getEntity().posX + event.getX(), box.maxY - event.getEntity().posY + event.getY(),
                    box.maxZ - event.getEntity().posZ + event.getZ());
            Color boundingBoxColor = colorSetting;
            RenderGlobal.drawOutlinedBoundingBox(offsetBox, boundingBoxColor.getRed(), boundingBoxColor.getGreen(), boundingBoxColor.getBlue(), (int) (alphaSetting * 255));
        }

        if (eyeHeightSetting && event.getEntity() instanceof EntityLivingBase) {
            RenderGlobal.drawOutlinedBoundingBox(
                    new AxisAlignedBB(event.getX() - half, event.getY() + event.getEntity().getEyeHeight() - 0.009999999776482582D,
                            event.getZ() - half, event.getX() + half,
                            event.getY() + event.getEntity().getEyeHeight() + 0.009999999776482582D, event.getZ() + half),
                    eyeHeightColor.getRed(), eyeHeightColor.getGreen(), eyeHeightColor.getBlue(),
                    (int) (alphaSetting * 255));
        }

        if (lookVectorSetting) {

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();

            Vec3 look = event.getEntity().getLook(event.getPartialTicks());
            worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(event.getX(), event.getY() + event.getEntity().getEyeHeight(), event.getZ()).color(0, 0, 255, 255)
                    .endVertex();
            worldrenderer.pos(event.getX() + look.xCoord * 2,
                            event.getY() + event.getEntity().getEyeHeight() + look.yCoord * 2, event.getZ() + look.zCoord * 2)
                    .color(lookVectorColor.getRed(), lookVectorColor.getGreen(), lookVectorColor.getBlue(), (int) (alphaSetting * 255)).endVertex();
            tessellator.draw();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (mc.getRenderManager() != null) {
            mc.getRenderManager().setDebugBoundingBox(true);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mc.getRenderManager() != null) {
            mc.getRenderManager().setDebugBoundingBox(false);
        }
    }
}
