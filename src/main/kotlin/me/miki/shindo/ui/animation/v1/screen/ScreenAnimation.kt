package me.miki.shindo.ui.animation.v1.screen

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.ui.animation.v1.GlobalAnimationSettings
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.opengl.GL11

/**
 * Framebuffer-backed screen effect that scales and fades rendered content using NanoVG.
 * Intended for UI transitions; caches framebuffer/paint in the base class to minimize allocations.
 */
open class ScreenAnimation : ScreenFramebufferBase(), ScreenEffect {

    /**
     * Renders [task] into an offscreen framebuffer and plays a scale/alpha animation before compositing.
     */
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
        val sr = ScaledResolution(mc) // Allocates per call; ScaledResolution lacks reuse hooks and is relatively cheap.
        val factor = sr.scaleFactor

        if (!GlobalAnimationSettings.enabled) {
            nvg.setupAndDraw(task)
            glRender?.run()
            return
        }

        ensureFramebuffer(nvg, mc.displayWidth, mc.displayHeight)

        val floaty = snapshotClearColor()
        GlStateManager.enableTexture2D()

        GL11.glClearColor(0f, 0f, 0f, 0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        restoreClearColor(floaty)

        nvg.setupAndDraw(Runnable {
            nvg.resetScissor()
            task.run()
            nvg.resetScissor()
        }) // Runnable allocated per invocation; unavoidable given NanoVG callback signature.
        glRender?.run()

        mc.framebuffer.bindFramebuffer(true)

        nvg.setupAndDraw(Runnable {
            nvg.resetScissor()
            nvg.setAlpha(alphaProgress.coerceAtMost(1.0f))
            nvg.scale(x * factor, y * factor, width * factor, height * factor, animationProgress)

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
                    sharedPaint
                )
            )
            NanoVG.nvgFill(nvg.getContext())
            nvg.resetScissor()
        }, false) // Runnable allocation per wrap call; retained to satisfy NanoVG API.
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
        disposeFramebuffer(nvg)
    }
}
