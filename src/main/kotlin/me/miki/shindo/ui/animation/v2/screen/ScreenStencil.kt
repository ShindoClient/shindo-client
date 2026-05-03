package me.miki.shindo.ui.animation.v2.screen

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.opengl.GL11


open class ScreenStencil : ScreenFramebufferBase(), ScreenEffect {

    fun wrap(
        task: Runnable,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        alpha: Float = 1f
    ) {
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager ?: return
        val sr = ScaledResolution(mc)
        val factor = sr.scaleFactor
        val ctx = nvg.getContext()

        ensureFramebuffer(nvg, mc.displayWidth, mc.displayHeight)

        val savedColor = snapshotClearColor()
        GL11.glClearColor(0f, 0f, 0f, 0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        restoreClearColor(savedColor)

        nvg.setupAndDraw(task)

        mc.framebuffer.bindFramebuffer(true)

        nvg.setupAndDraw(Runnable {
            NanoVG.nvgGlobalAlpha(ctx, alpha.coerceIn(0f, 1f))
            NanoVG.nvgBeginPath(ctx)
            NanoVG.nvgRoundedRect(
                ctx,
                x * factor, y * factor,
                width * factor, height * factor,
                radius * factor
            )
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
