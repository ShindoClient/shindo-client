package me.miki.shindo.ui.animation.v1.screen

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.opengl.GL11

/**
 * Screen masking helper that composites offscreen content through a rounded-rectangle stencil.
 */
open class ScreenStencil : ScreenFramebufferBase(), ScreenEffect {

    /**
     * Draws [task] into an offscreen framebuffer and re-composites it through a rounded-rect stencil.
     */
    fun wrap(task: Runnable, x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Float = 1f) {
        val sr = ScaledResolution(mc) // Allocates per call; no cache available on 1.8.9 client.
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager ?: return
        val factor = sr.scaleFactor

        ensureFramebuffer(nvg, mc.displayWidth, mc.displayHeight)

        val floaty = snapshotClearColor()
        NanoVG.nvgBeginPath(nvg.getContext())
        GL11.glClearColor(0f, 0f, 0f, 0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        restoreClearColor(floaty)

        nvg.setupAndDraw(task)

        mc.framebuffer.bindFramebuffer(true)

        nvg.setupAndDraw(Runnable {
            NanoVG.nvgGlobalAlpha(nvg.getContext(), alpha)
            NanoVG.nvgBeginPath(nvg.getContext())
            NanoVG.nvgRoundedRect(
                nvg.getContext(),
                x * factor,
                y * factor,
                width * factor,
                height * factor,
                radius * factor
            )
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
        }, false) // Runnable allocation per wrap call; required by NanoVG callback style.
    }

    fun wrap(task: Runnable, x: Float, y: Float, width: Float, height: Float, radius: Float) =
        wrap(task, x, y, width, height, radius, 1f)

    override fun close() {
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager ?: return
        disposeFramebuffer(nvg)
    }
}
