package me.miki.shindo.ui.animation.screen

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.ui.animation.GlobalAnimationSettings
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.nanovg.NVGLUFramebuffer
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.nanovg.NanoVGGL2
import org.lwjgl.opengl.GL11
import org.lwjgl3.BufferUtils
import java.nio.FloatBuffer

open class ScreenAnimation : ScreenEffect {

    private val mc: Minecraft = Minecraft.getMinecraft()
    private var fbWidth: Int = 0
    private var fbHeight: Int = 0
    private var fb: NVGLUFramebuffer? = null

    fun wrap(
        glRender: Runnable?,
        task: Runnable,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        animationProgress: Float,
        alphaProgress: Float,
        stencil: Boolean
    ) {
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager ?: return
        val sr = ScaledResolution(mc)
        val factor = sr.scaleFactor

        if (!GlobalAnimationSettings.enabled) {
            nvg.setupAndDraw(task)
            glRender?.run()
            return
        }

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
        GlStateManager.enableTexture2D()
        GL11.glGetFloat(GL11.GL_COLOR_CLEAR_VALUE, floaty)

        GL11.glClearColor(0f, 0f, 0f, 0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        GL11.glClearColor(floaty.get(0), floaty.get(1), floaty.get(2), floaty.get(3))

        nvg.setupAndDraw(Runnable {
            nvg.resetScissor()
            task.run()
            nvg.resetScissor()
        })
        glRender?.run()

        mc.framebuffer.bindFramebuffer(true)

        nvg.setupAndDraw(Runnable {
            nvg.resetScissor()
            nvg.setAlpha(alphaProgress.coerceAtMost(1.0f))
            nvg.scale(x * factor, y * factor, width * factor, height * factor, animationProgress)

            val paint = NVGPaint.create()
            NanoVG.nvgBeginPath(nvg.getContext())

            if (stencil) {
                NanoVG.nvgRect(nvg.getContext(), x * factor, y * factor, width * factor, height * factor)
            } else {
                NanoVG.nvgRect(nvg.getContext(), 0f, 0f, mc.displayWidth.toFloat(), mc.displayHeight.toFloat())
            }

            NanoVG.nvgFillPaint(
                nvg.getContext(),
                NanoVG.nvgImagePattern(
                    nvg.getContext(),
                    0f, 0f,
                    mc.displayWidth.toFloat(),
                    mc.displayHeight.toFloat(),
                    0f,
                    fb!!.image(),
                    1f,
                    paint
                )
            )
            NanoVG.nvgFill(nvg.getContext())
            nvg.resetScissor()
        }, false)
    }

    fun wrap(
        glRender: Runnable?,
        task: Runnable,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        animationProgress: Float,
        alphaProgress: Float
    ) = wrap(glRender, task, x, y, width, height, animationProgress, alphaProgress, false)

    fun wrap(task: Runnable, animationProgress: Float, alphaProgress: Float) {
        val sr = ScaledResolution(mc)
        wrap(
            null,
            task,
            0f,
            0f,
            sr.scaledWidth.toFloat(),
            sr.scaledHeight.toFloat(),
            animationProgress,
            alphaProgress,
            false
        )
    }

    fun wrap(task: Runnable, progress: Float) {
        val sr = ScaledResolution(mc)
        wrap(null, task, 0f, 0f, sr.scaledWidth.toFloat(), sr.scaledHeight.toFloat(), progress, progress, false)
    }

    fun wrap(task: Runnable, x: Float, y: Float, width: Float, height: Float, progress: Float) =
        wrap(null, task, x, y, width, height, progress, progress, false)

    fun wrap(
        task: Runnable,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        animationProgress: Float,
        alphaProgress: Float
    ) =
        wrap(null, task, x, y, width, height, animationProgress, alphaProgress, false)

    fun wrap(
        task: Runnable,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        animationProgress: Float,
        alphaProgress: Float,
        stencil: Boolean
    ) =
        wrap(null, task, x, y, width, height, animationProgress, alphaProgress, stencil)

    fun wrap(task: Runnable, x: Int, y: Int, width: Int, height: Int, progress: Float) =
        wrap(null, task, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), progress, progress, false)

    fun wrap(task: Runnable, x: Int, y: Int, width: Int, height: Int, animationProgress: Float, alphaProgress: Float) =
        wrap(
            null,
            task,
            x.toFloat(),
            y.toFloat(),
            width.toFloat(),
            height.toFloat(),
            animationProgress,
            alphaProgress,
            false
        )

    fun wrap(
        task: Runnable,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        animationProgress: Float,
        alphaProgress: Float,
        stencil: Boolean
    ) =
        wrap(
            null,
            task,
            x.toFloat(),
            y.toFloat(),
            width.toFloat(),
            height.toFloat(),
            animationProgress,
            alphaProgress,
            stencil
        )

    override fun close() {
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager ?: return
        fb?.let {
            NanoVGGL2.nvgluDeleteFramebuffer(nvg.getContext(), it)
            fb = null
        }
    }
}
