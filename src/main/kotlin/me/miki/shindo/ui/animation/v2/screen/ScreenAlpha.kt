package me.miki.shindo.ui.animation.v2.screen

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.opengl.GL11

open class ScreenAlpha : ScreenFramebufferBase(), ScreenEffect {

    fun wrap(task: Runnable, alpha: Float) {
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager ?: return

        ensureFramebuffer(nvg, mc.displayWidth, mc.displayHeight)

        val savedColor = snapshotClearColor()
        GL11.glClearColor(0f, 0f, 0f, 0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        restoreClearColor(savedColor)

        nvg.setupAndDraw(task)

        mc.framebuffer.bindFramebuffer(true)

        val clampedAlpha = alpha.coerceIn(0f, 1f)
        val ctx = nvg.getContext()
        nvg.setupAndDraw(Runnable {
            nvg.setAlpha(clampedAlpha)
            NanoVG.nvgBeginPath(ctx)
            NanoVG.nvgRect(ctx, 0f, 0f, mc.displayWidth.toFloat(), mc.displayHeight.toFloat())
            NanoVG.nvgFillPaint(
                ctx,
                NanoVG.nvgImagePattern(ctx, 0f, 0f,
                    mc.displayWidth.toFloat(), mc.displayHeight.toFloat(),
                    0f, fb!!.image(), 1f, sharedPaint)
            )
            NanoVG.nvgFill(ctx)
        }, false)
    }

    override fun close() {
        releaseFramebuffer(Shindo.getInstance().nanoVGManager ?: return)
    }
}
