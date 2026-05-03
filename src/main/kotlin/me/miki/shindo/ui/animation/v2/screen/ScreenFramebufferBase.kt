package me.miki.shindo.ui.animation.v2.screen

import me.miki.shindo.management.nanovg.NanoVGManager
import net.minecraft.client.Minecraft
import org.lwjgl.nanovg.NVGLUFramebuffer
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVGGL2
import org.lwjgl.opengl.GL11
import org.lwjgl3.BufferUtils
import java.nio.FloatBuffer


open class ScreenFramebufferBase {

    protected val mc: Minecraft = Minecraft.getMinecraft()

    private var fbWidth: Int = 0
    private var fbHeight: Int = 0
    protected var fb: NVGLUFramebuffer? = null

    private val clearColorSnapshot: FloatBuffer = BufferUtils.createFloatBuffer(4)
    protected val sharedPaint: NVGPaint = NVGPaint.create()

    protected fun ensureFramebuffer(nvg: NanoVGManager, width: Int, height: Int) {
        if (fb == null || fbWidth != width || fbHeight != height) {
            releaseFramebuffer(nvg)
            fb = NanoVGGL2.nvgluCreateFramebuffer(nvg.getContext(), width, height, 0)
            fbWidth = width
            fbHeight = height
        }
        NanoVGGL2.nvgluBindFramebuffer(nvg.getContext(), fb)
        GL11.glViewport(0, 0, width, height)
    }

    protected fun releaseFramebuffer(nvg: NanoVGManager) {
        fb?.let { NanoVGGL2.nvgluDeleteFramebuffer(nvg.getContext(), it) }
        fb = null
    }

    protected fun snapshotClearColor(): FloatBuffer {
        clearColorSnapshot.clear()
        GL11.glGetFloat(GL11.GL_COLOR_CLEAR_VALUE, clearColorSnapshot)
        clearColorSnapshot.rewind()
        return clearColorSnapshot
    }

    protected fun restoreClearColor(buf: FloatBuffer) {
        GL11.glClearColor(buf.get(0), buf.get(1), buf.get(2), buf.get(3))
    }
}
