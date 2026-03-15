package me.miki.shindo.ui.animation.screen

import me.miki.shindo.management.nanovg.NanoVGManager
import net.minecraft.client.Minecraft
import org.lwjgl.nanovg.NVGLUFramebuffer
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVGGL2
import org.lwjgl.opengl.GL11
import org.lwjgl3.BufferUtils
import java.nio.FloatBuffer

/**
 * Internal helper that caches NanoVG framebuffer resources to avoid per-frame allocations.
 * Not part of the public API; only used by screen animation effects.
 */
open class ScreenFramebufferBase {
    protected val mc: Minecraft = Minecraft.getMinecraft()
    protected var fbWidth: Int = 0
    protected var fbHeight: Int = 0
    protected var fb: NVGLUFramebuffer? = null
    protected val clearColorBuffer: FloatBuffer = BufferUtils.createFloatBuffer(16)
    protected val sharedPaint: NVGPaint = NVGPaint.create()

    protected fun ensureFramebuffer(nvg: NanoVGManager, width: Int, height: Int) {
        if (fb == null || fbWidth != width || fbHeight != height) {
            disposeFramebuffer(nvg)
            // Framebuffer is only recreated when the window size changes, avoiding per-frame GL allocations.
            fb = NanoVGGL2.nvgluCreateFramebuffer(nvg.getContext(), width, height, 0)
            fbWidth = width
            fbHeight = height
        }
        NanoVGGL2.nvgluBindFramebuffer(nvg.getContext(), fb)
        GL11.glViewport(0, 0, width, height)
    }

    protected fun snapshotClearColor(): FloatBuffer {
        clearColorBuffer.clear()
        GL11.glGetFloat(GL11.GL_COLOR_CLEAR_VALUE, clearColorBuffer)
        clearColorBuffer.rewind()
        return clearColorBuffer
    }

    protected fun restoreClearColor(buffer: FloatBuffer) {
        GL11.glClearColor(buffer.get(0), buffer.get(1), buffer.get(2), buffer.get(3))
    }

    protected fun disposeFramebuffer(nvg: NanoVGManager) {
        fb?.let { NanoVGGL2.nvgluDeleteFramebuffer(nvg.getContext(), it) }
        fb = null
    }
}
