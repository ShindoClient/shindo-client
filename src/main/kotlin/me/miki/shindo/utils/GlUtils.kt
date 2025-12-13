package me.miki.shindo.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer

object GlUtils {

    private val mc: Minecraft = Minecraft.getMinecraft()

    @JvmStatic
    fun bindTexture(texture: Int) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture)
    }

    @JvmStatic
    fun getTexImage(i: Int, j: Int, k: Int, l: Int, buffer: ByteBuffer) {
        GL11.glGetTexImage(i, j, k, l, buffer)
    }

    @JvmStatic
    fun pixelStore(i: Int, j: Int) {
        GL11.glPixelStorei(i, j)
    }

    @JvmStatic
    fun startScale(x: Float, y: Float, scale: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
        GlStateManager.scale(scale, scale, 1f)
        GlStateManager.translate(-x, -y, 0f)
    }

    @JvmStatic
    fun startScale(x: Float, y: Float, width: Float, height: Float, scale: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.translate((x + (x + width)) / 2, (y + (y + height)) / 2, 0f)
        GlStateManager.scale(scale, scale, 1f)
        GlStateManager.translate(-(x + (x + width)) / 2, -(y + (y + height)) / 2, 0f)
    }

    @JvmStatic
    fun stopScale() {
        GlStateManager.popMatrix()
    }

    @JvmStatic
    fun startTranslate(x: Float, y: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
    }

    @JvmStatic
    fun stopTranslate() {
        GlStateManager.popMatrix()
    }

    @JvmStatic
    fun createFrameBuffer(framebuffer: Framebuffer?): Framebuffer {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            framebuffer?.deleteFramebuffer()
            return Framebuffer(mc.displayWidth, mc.displayHeight, true)
        }
        return framebuffer
    }

    @JvmStatic
    fun setAlphaLimit(limit: Float) {
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(GL11.GL_GREATER, (limit * 0.01f))
    }
}
