package me.miki.shindo.utils.render

import me.miki.shindo.utils.ColorUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11
import java.awt.Color

object RenderUtils {

    private val mc: Minecraft = Minecraft.getMinecraft()

    @JvmStatic
    fun connectPoints(xOne: Float, yOne: Float, xTwo: Float, yTwo: Float) {
        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.8f)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glLineWidth(0.5f)
        GL11.glBegin(GL11.GL_LINES)
        GL11.glVertex2f(xOne, yOne)
        GL11.glVertex2f(xTwo, yTwo)
        GL11.glEnd()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glPopMatrix()
    }

    @JvmStatic
    fun drawCircle(x: Float, y: Float, radius: Float, color: Int) {
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        GL11.glColor4f(red, green, blue, alpha)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glPushMatrix()
        GL11.glLineWidth(1f)
        GL11.glBegin(GL11.GL_POLYGON)

        for (i in 0..360) {
            GL11.glVertex2d(
                x + Math.sin(i * Math.PI / 180.0) * radius,
                y + Math.cos(i * Math.PI / 180.0) * radius
            )
        }

        GL11.glEnd()
        GL11.glPopMatrix()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glColor4f(1f, 1f, 1f, 1f)
    }

    @JvmStatic
    fun drawItemStack(stack: ItemStack, x: Int, y: Int) {
        GlStateManager.pushMatrix()
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.disableAlpha()
        GlStateManager.clear(256)
        mc.renderItem.zLevel = -150.0f
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableBlend()
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.enableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
        mc.renderItem.renderItemIntoGUI(stack, x, y)
        mc.renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, stack, x, y, null)
        mc.renderItem.zLevel = 0.0f
        GlStateManager.enableAlpha()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.popMatrix()
    }

    @JvmStatic
    fun drawScaledCustomSizeModalRect(
        x: Double,
        y: Double,
        u: Float,
        v: Float,
        uWidth: Int,
        vHeight: Int,
        width: Double,
        height: Double,
        tileWidth: Float,
        tileHeight: Float
    ) {
        val f = 1.0f / tileWidth
        val f1 = 1.0f / tileHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x, y + height, 0.0).tex(u * f, (v + vHeight.toFloat()) * f1).endVertex()
        worldrenderer.pos(x + width, y + height, 0.0).tex((u + uWidth.toFloat()) * f, (v + vHeight.toFloat()) * f1)
            .endVertex()
        worldrenderer.pos(x + width, y, 0.0).tex((u + uWidth.toFloat()) * f, v * f1).endVertex()
        worldrenderer.pos(x, y, 0.0).tex(u * f, v * f1).endVertex()
        tessellator.draw()
    }

    @JvmStatic
    fun drawRect(x: Float, y: Float, width: Float, height: Float, color: Color) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)

        GL11.glPushMatrix()
        ColorUtils.setColor(color.rgb)
        GL11.glBegin(7)
        GL11.glVertex2d((x + width).toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), (y + height).toDouble())
        GL11.glVertex2d((x + width).toDouble(), (y + height).toDouble())
        GL11.glEnd()
        GL11.glPopMatrix()

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(2848)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        ColorUtils.resetColor()
    }

    @JvmStatic
    fun drawOutline(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, color: Color) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)

        GL11.glPushMatrix()
        ColorUtils.setColor(color.rgb)
        GL11.glLineWidth(lineWidth)
        GL11.glBegin(1)
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), (y + height).toDouble())
        GL11.glVertex2d((x + width).toDouble(), (y + height).toDouble())
        GL11.glVertex2d((x + width).toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d((x + width).toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), (y + height).toDouble())
        GL11.glVertex2d((x + width).toDouble(), (y + height).toDouble())
        GL11.glEnd()
        GL11.glPopMatrix()

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(2848)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        ColorUtils.resetColor()
    }

    @JvmStatic
    fun drawTexturedModalRect(x: Int, y: Int, textureX: Int, textureY: Int, width: Int, height: Int) {
        val f = 0.00390625f
        val f1 = 0.00390625f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex(textureX.toFloat() * f, (textureY + height).toFloat() * f1).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex((textureX + width).toFloat() * f, (textureY + height).toFloat() * f1).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex((textureX + width).toFloat() * f, textureY.toFloat() * f1).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(textureX.toFloat() * f, textureY.toFloat() * f1)
            .endVertex()
        tessellator.draw()
    }

    @JvmStatic
    fun drawQuads(x: Float, y: Float, width: Float, height: Float) {
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0f, 0f)
        GL11.glVertex2f(x, y)
        GL11.glTexCoord2f(0f, 1f)
        GL11.glVertex2f(x, y + height)
        GL11.glTexCoord2f(1f, 1f)
        GL11.glVertex2f(x + width, y + height)
        GL11.glTexCoord2f(1f, 0f)
        GL11.glVertex2f(x + width, y)
        GL11.glEnd()
    }

    @JvmStatic
    fun drawModalRectWithCustomSizedTexture(
        x: Float,
        y: Float,
        u: Float,
        v: Float,
        width: Int,
        height: Int,
        textureWidth: Float,
        textureHeight: Float
    ) {
        val f = 1.0f / textureWidth
        val f1 = 1.0f / textureHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(u * f, (v + height.toFloat()) * f1).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex((u + width.toFloat()) * f, (v + height.toFloat()) * f1).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0).tex((u + width.toFloat()) * f, v * f1)
            .endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(u * f, v * f1).endVertex()
        tessellator.draw()
    }

    @JvmStatic
    fun fill(x1: Int, y1: Int, x2: Int, y2: Int, c: Color) {
        drawRect(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), c)
    }

    @JvmStatic
    fun hLine(startX: Int, endX: Int, y: Int, c: Color) {
        var sX = startX
        var eX = endX
        if (eX < sX) {
            val i = sX
            sX = eX
            eX = i
        }
        drawRect(sX.toFloat(), y.toFloat(), (eX + 1).toFloat(), (y + 1).toFloat(), c)
    }

    @JvmStatic
    fun vLine(x: Int, startY: Int, endY: Int, c: Color) {
        var sY = startY
        var eY = endY
        if (eY < sY) {
            val i = sY
            sY = eY
            eY = i
        }
        drawRect(x.toFloat(), (sY + 1).toFloat(), (x + 1).toFloat(), eY.toFloat(), c)
    }

    // barra topo/bottom com leve fade
    @JvmStatic
    fun gradientBar(x1: Int, y1: Int, x2: Int, y2: Int, a: Color, b: Color) {
        val t = Tessellator.getInstance()
        val wr: WorldRenderer = t.worldRenderer
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_ALPHA_TEST)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glShadeModel(GL11.GL_SMOOTH)

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        wr.pos(x2.toDouble(), y1.toDouble(), 0.0).color(a.red, a.green, a.blue, a.alpha).endVertex()
        wr.pos(x1.toDouble(), y1.toDouble(), 0.0).color(a.red, a.green, a.blue, a.alpha).endVertex()
        wr.pos(x1.toDouble(), y2.toDouble(), 0.0).color(b.red, b.green, b.blue, b.alpha).endVertex()
        wr.pos(x2.toDouble(), y2.toDouble(), 0.0).color(b.red, b.green, b.blue, b.alpha).endVertex()
        t.draw()

        GL11.glShadeModel(GL11.GL_FLAT)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }
}
