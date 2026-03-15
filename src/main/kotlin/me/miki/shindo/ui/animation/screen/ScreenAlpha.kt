package me.miki.shindo.ui.animation.screen

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.opengl.GL11

open class ScreenAlpha : ScreenFramebufferBase(), ScreenEffect {

    /**
     * Renders [task] offscreen and composites it with an alpha fade using cached buffers.
     */
    fun wrap(task: Runnable, alphaProgress: Float) {
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager ?: return

        ensureFramebuffer(nvg, mc.displayWidth, mc.displayHeight)

        val floaty = snapshotClearColor()

        GL11.glClearColor(0f, 0f, 0f, 0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        restoreClearColor(floaty)

        nvg.setupAndDraw(task)

        mc.framebuffer.bindFramebuffer(true)

        nvg.setupAndDraw(Runnable {
            nvg.setAlpha(alphaProgress.coerceAtMost(1.0f))

            NanoVG.nvgBeginPath(nvg.getContext())
            NanoVG.nvgRect(nvg.getContext(), 0f, 0f, mc.displayWidth.toFloat(), mc.displayHeight.toFloat())
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
        }, false)
    }

    override fun close() {
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager ?: return
        disposeFramebuffer(nvg)
    }
}
