package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender3D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ChunkBordersMod extends Mod {

    public ChunkBordersMod() {
        super(TranslateText.CHUNK_BORDERS, TranslateText.CHUNK_BORDERS_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {

        EntityPlayerSP entity = Minecraft.getMinecraft().thePlayer;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        float frame = event.getPartialTicks();
        double inChunkPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) frame;
        double inChunkPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) frame;
        double inChunkPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) frame;

        double x = 0.0D;
        double z = 0.0D;

        Color color = Color.BLUE;
        Color color2 = Color.YELLOW;

        int eyeHeightBlock;

        GL11.glPushMatrix();

        GL11.glTranslated(-inChunkPosX, -inChunkPosY, -inChunkPosZ);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(1F);
        worldRenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        GL11.glTranslatef((float) (entity.chunkCoordX * 16), 0.0F, (float) (entity.chunkCoordZ * 16));

        for (int eyeHeight = -2; eyeHeight <= 2; ++eyeHeight) {
            for (eyeHeightBlock = -2; eyeHeightBlock <= 2; ++eyeHeightBlock) {
                if (Math.abs(eyeHeightBlock) != 2 || Math.abs(eyeHeight) != 2) {
                    x = eyeHeightBlock * 16;
                    z = eyeHeight * 16;

                    worldRenderer.pos(x, 0.0D, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                    worldRenderer.pos(x, 256.0D, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                    worldRenderer.pos(x + 16.0D, 0.0D, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                    worldRenderer.pos(x + 16.0D, 256.0D, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                    worldRenderer.pos(x, 0.0D, z + 16.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                    worldRenderer.pos(x, 256.0D, z + 16.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                    worldRenderer.pos(x + 16.0D, 0.0D, z + 16.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                    worldRenderer.pos(x + 16.0D, 256.0D, z + 16.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                }
            }
        }

        z = 0.0D;
        x = 0.0D;

        float f = (float) ((double) entity.getEyeHeight() + entity.posY);

        eyeHeightBlock = (int) Math.floor(f);

        for (int y = 0; y < 257; ++y) {
            if (y < 256) {
                for (int n = 1; n < 16; ++n) {
                    worldRenderer.pos(n, y, z).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
                    worldRenderer.pos(n, y + 1, z).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
                    worldRenderer.pos(x, y, n).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
                    worldRenderer.pos(x, y + 1, n).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
                    worldRenderer.pos(n, y, z + 16.0D).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
                    worldRenderer.pos(n, y + 1, z + 16.0D).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
                    worldRenderer.pos(x + 16.0D, y, n).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
                    worldRenderer.pos(x + 16.0D, y + 1, n).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
                }
            }

            worldRenderer.pos(0.0D, y, 0.0D).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
            worldRenderer.pos(16.0D, y, 0.0D).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
            worldRenderer.pos(0.0D, y, 0.0D).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
            worldRenderer.pos(0.0D, y, 16.0D).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
            worldRenderer.pos(16.0D, y, 0.0D).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
            worldRenderer.pos(16.0D, y, 16.0D).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
            worldRenderer.pos(0.0D, y, 16.0D).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
            worldRenderer.pos(16.0D, y, 16.0D).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).endVertex();
        }

        tessellator.draw();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
    }
}
