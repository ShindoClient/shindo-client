package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender3D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.utils.ColorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Iterator;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class ReachCirclesMod extends Mod {

    @Property(type = PropertyType.NUMBER, translate = TranslateText.LINE_WIDTH, min = 1, max = 5, current = 2, step = 1)
    private int lineWidthSetting = 2;

    public ReachCirclesMod() {
        super(TranslateText.REACH_CIRCLES, TranslateText.REACH_CIRCLES_DESCRIPTION, ModCategory.PLAYER);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {

        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(2929);
        GL11.glEnable(2848);
        GL11.glDepthMask(false);
        Iterator<Entity> iterator = mc.theWorld.loadedEntityList.iterator();

        while (iterator.hasNext()) {
            Object o = iterator.next();
            Entity entity = (Entity) o;

            if (entity instanceof EntityLivingBase && !entity.isInvisible() && !entity.isSneaking() && entity != mc.thePlayer && ((EntityLivingBase) entity).canEntityBeSeen(mc.thePlayer) && !entity.isInvisible() && entity instanceof EntityPlayer) {
                double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) event.getPartialTicks() - mc.getRenderManager().viewerPosX;
                double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) event.getPartialTicks() - mc.getRenderManager().viewerPosY;
                double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) event.getPartialTicks() - mc.getRenderManager().viewerPosZ;

                this.circle(posX, posY, posZ, mc.playerController.isInCreativeMode() ? 4.7D : 3.4D);
            }
        }

        GL11.glDepthMask(true);
        GL11.glDisable(2848);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glPopMatrix();
    }

    private void circle(double x, double y, double z, double rad) {

        AccentColor currentColor = Shindo.getInstance().getColorManager().getCurrentColor();

        GL11.glPushMatrix();
        Color color = ColorUtils.applyAlpha(currentColor.getInterpolateColor(), 120);

        GL11.glLineWidth(lineWidthSetting);
        ColorUtils.setColor(color.getRGB());
        GL11.glBegin(1);

        for (int i = 0; i <= 90; ++i) {
            ColorUtils.setColor(color.getRGB(), 0.4F);
            GL11.glVertex3d(x + rad * Math.cos((double) i * 6.283185307179586D / 45.0D), y, z + rad * Math.sin((double) i * 6.283185307179586D / 45.0D));
        }

        GL11.glEnd();
        GL11.glPopMatrix();
    }
}
