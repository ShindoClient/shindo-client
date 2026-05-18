package me.miki.shindo.ui.animation.v2.screen

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import net.minecraft.client.Minecraft
import org.lwjgl.nanovg.NVGLUFramebuffer
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.nanovg.NanoVGGL2
import org.lwjgl.opengl.GL11
import org.lwjgl3.BufferUtils
import java.nio.FloatBuffer
import kotlin.math.min

class ScreenAlpha {
    private val mc: Minecraft = Minecraft.getMinecraft()

    private var fbWidth = 0
    private var fbHeight = 0
    private var fb: NVGLUFramebuffer? = null

    fun wrap(
        task: Runnable?,
        alphaProgress: Float,
    ) {
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager!!

        if (fbWidth != mc.displayWidth || fbHeight != mc.displayHeight) {
            close()
        }

        if (fb == null) {
            fbWidth = mc.displayWidth
            fbHeight = mc.displayHeight
            fb = NanoVGGL2.nvgluCreateFramebuffer(nvg.getContext(), mc.displayWidth, mc.displayHeight, 0)
        }

        NanoVGGL2.nvgluBindFramebuffer(nvg.getContext(), fb)

        GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight)

        val floaty: FloatBuffer = BufferUtils.createFloatBuffer(16)
        GL11.glGetFloat(GL11.GL_COLOR_CLEAR_VALUE, floaty)

        GL11.glClearColor(0f, 0f, 0f, 0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)

        GL11.glClearColor(floaty.get(0), floaty.get(1), floaty.get(2), floaty.get(3))

        nvg.setupAndDraw(task)

        mc.framebuffer.bindFramebuffer(true)

        nvg.setupAndDraw(
            Runnable {
                nvg.setAlpha(min(alphaProgress, 1.0f))
                val paint = NVGPaint.create()

                NanoVG.nvgBeginPath(nvg.getContext())

                NanoVG.nvgRect(nvg.getContext(), 0f, 0f, mc.displayWidth.toFloat(), mc.displayHeight.toFloat())

                NanoVG.nvgFillPaint(
                    nvg.getContext(),
                    NanoVG.nvgImagePattern(
                        nvg.getContext(),
                        0f,
                        0f,
                        mc.displayWidth.toFloat(),
                        mc.displayHeight.toFloat(),
                        0f,
                        fb!!.image(),
                        1f,
                        paint,
                    ),
                )
                NanoVG.nvgFill(nvg.getContext())
            },
            false,
        )
    }

    fun close() {
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager!!

        if (fb != null) {
            NanoVGGL2.nvgluDeleteFramebuffer(nvg.getContext(), fb!!)
            fb = null
        }
    }
}