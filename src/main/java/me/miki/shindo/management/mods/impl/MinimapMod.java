package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventLoadWorld;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.HUDMod;
import me.miki.shindo.management.mods.impl.minimap.ChunkAtlas;
import me.miki.shindo.management.mods.impl.minimap.ChunkTile;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.utils.GlUtils;
import me.miki.shindo.utils.buffer.ScreenStencil;
import me.miki.shindo.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class MinimapMod extends HUDMod {

    @Property(type = PropertyType.NUMBER, translate = TranslateText.WIDTH, min = 10, max = 180, current = 150, step = 1)
    private int widthSetting = 150;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.HEIGHT, min = 10, max = 180, current = 70, step = 1)
    private int heightSetting = 70;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.ALPHA, min = 0.0F, max = 1F, current = 1F)
    private double alphaSetting = 1F;

    private final ScreenStencil stencil = new ScreenStencil();
    private ChunkAtlas chunkAtlas;

    public MinimapMod() {
        super(TranslateText.MINIMAP, TranslateText.MINIMAP_DESCRIPTION);
    }

    @Override
    public void setup() {
        chunkAtlas = new ChunkAtlas(10);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        int width = widthSetting;
        int height = heightSetting;

        nvg.setupAndDraw(() -> {
            nvg.drawShadow(this.getX(), this.getY(), width * this.getScale(), height * this.getScale(), 6 * this.getScale());
        });

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        stencil.wrap(() -> drawMap(event.getPartialTicks()), this.getX(), this.getY(), width * this.getScale(), height * this.getScale(), 6 * this.getScale(), (float) alphaSetting);

        this.setWidth(width);
        this.setHeight(height);
    }

    private void drawMap(float partialTicks) {

        int width = widthSetting;
        int height = heightSetting;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        EntityPlayer p = mc.thePlayer;

        double x = lerp(p.prevPosX, p.posX, partialTicks);
        double z = lerp(p.prevPosZ, p.posZ, partialTicks);
        double yaw = lerp(p.prevRotationYaw, p.rotationYaw, partialTicks);

        chunkAtlas.loadChunks((int) x >> 4, (int) z >> 4);

        RenderUtils.drawRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), new Color(138, 176, 254));

        GlUtils.startTranslate(this.getX() + (width / 2) * this.getScale(), this.getY() + (height / 2) * this.getScale());

        GL11.glRotated(180 - yaw, 0, 0, 1);

        GlStateManager.color(1F, 1F, 1F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.bindTexture(chunkAtlas.getTextureHandle());

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        double chunkWidth = chunkAtlas.getSpriteWidth();
        double chunkHeight = chunkAtlas.getSpriteHeight();

        for (ChunkTile sprite : chunkAtlas) {

            double minX = chunkAtlas.getSpriteX(sprite.getOffset());
            double minY = chunkAtlas.getSpriteY(sprite.getOffset());

            double maxX = minX + chunkWidth;
            double maxY = minY + chunkHeight;

            double renderX = (sprite.getChunkX() << 4) - x;
            double renderY = (sprite.getChunkZ() << 4) - z;

            worldRenderer.pos(renderX, renderY, 0).tex(minX, minY).endVertex();
            worldRenderer.pos(renderX, renderY + 16, 0).tex(minX, maxY).endVertex();
            worldRenderer.pos(renderX + 16, renderY + 16, 0).tex(maxX, maxY).endVertex();
            worldRenderer.pos(renderX + 16, renderY + 0, 0).tex(maxX, minY).endVertex();
        }

        tessellator.draw();

        GlUtils.stopTranslate();
    }

    @EventTarget
    public void onLoadWorld(EventLoadWorld event) {
        chunkAtlas.clear();
    }

    private double lerp(double prev, double current, float partialTicks) {
        return prev + (current - prev) * partialTicks;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        chunkAtlas.clear();
    }
}
