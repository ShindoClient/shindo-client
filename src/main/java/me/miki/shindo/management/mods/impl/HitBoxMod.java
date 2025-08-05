package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRenderHitbox;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.ColorSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;
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

public class HitBoxMod extends Mod {

    private final Color eyeHeightColor = Color.RED;
    private final Color lookVectorColor = Color.BLUE;

    private final ColorSetting colorSetting = new ColorSetting(TranslateText.COLOR, this, new Color(255, 255, 255), false);
    private final NumberSetting alphaSetting = new NumberSetting(TranslateText.ALPHA, this, 1, 0, 1.0, false);
    private final BooleanSetting boundingBoxSetting = new BooleanSetting(TranslateText.BOUNDING_BOX, this, true);
    private final BooleanSetting eyeHeightSetting = new BooleanSetting(TranslateText.EYE_HEIGHT, this, true);
    private final BooleanSetting lookVectorSetting = new BooleanSetting(TranslateText.LOOK_VECTOR, this, true);

    private final NumberSetting lineWidthSetting = new NumberSetting(TranslateText.LINE_WIDTH, this, 2, 1, 5, true);

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
        GL11.glLineWidth(lineWidthSetting.getValueFloat());

        if (boundingBoxSetting.isToggled()) {
            AxisAlignedBB box = event.getEntity().getEntityBoundingBox();
            AxisAlignedBB offsetBox = new AxisAlignedBB(box.minX - event.getEntity().posX + event.getX(),
                    box.minY - event.getEntity().posY + event.getY(), box.minZ - event.getEntity().posZ + event.getZ(),
                    box.maxX - event.getEntity().posX + event.getX(), box.maxY - event.getEntity().posY + event.getY(),
                    box.maxZ - event.getEntity().posZ + event.getZ());
            Color boundingBoxColor = colorSetting.getColor();
            RenderGlobal.drawOutlinedBoundingBox(offsetBox, boundingBoxColor.getRed(), boundingBoxColor.getGreen(), boundingBoxColor.getBlue(), (int) (alphaSetting.getValue() * 255));
        }

        if (eyeHeightSetting.isToggled() && event.getEntity() instanceof EntityLivingBase) {
            RenderGlobal.drawOutlinedBoundingBox(
                    new AxisAlignedBB(event.getX() - half, event.getY() + event.getEntity().getEyeHeight() - 0.009999999776482582D,
                            event.getZ() - half, event.getX() + half,
                            event.getY() + event.getEntity().getEyeHeight() + 0.009999999776482582D, event.getZ() + half),
                    eyeHeightColor.getRed(), eyeHeightColor.getGreen(), eyeHeightColor.getBlue(),
                    (int) (alphaSetting.getValue() * 255));
        }

        if (lookVectorSetting.isToggled()) {

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();

            Vec3 look = event.getEntity().getLook(event.getPartialTicks());
            worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(event.getX(), event.getY() + event.getEntity().getEyeHeight(), event.getZ()).color(0, 0, 255, 255)
                    .endVertex();
            worldrenderer.pos(event.getX() + look.xCoord * 2,
                            event.getY() + event.getEntity().getEyeHeight() + look.yCoord * 2, event.getZ() + look.zCoord * 2)
                    .color(lookVectorColor.getRed(), lookVectorColor.getGreen(), lookVectorColor.getBlue(), (int) (alphaSetting.getValue() * 255)).endVertex();
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
