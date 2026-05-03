package me.miki.shindo.ui.animation.v2.screen

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.ui.animation.v2.core.GlobalAnimationSettings
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.opengl.GL11

open class ScreenAnimation : ScreenFramebufferBase(), ScreenEffect {

    /**
     * Core wrap — all other overloads delegate here.
     *
     * @param x           Region origin X in GUI coordinates (default 0).
     * @param y           Region origin Y in GUI coordinates (default 0).
     * @param width       Region width  in GUI coordinates (default full screen).
     * @param height      Region height in GUI coordinates (default full screen).
     * @param animProgress Scale progress [0..1]; 1 = normal size.
     * @param alphaProgress Alpha  progress [0..1]; 1 = fully opaque.
     * @param stencil     When true, clips compositing to the specified region rect.
     * @param glRender    Optional extra GL render call executed after [task] (default null).
     * @param task        Lambda containing your NanoVG draw calls.
     */
    fun wrap(
        glRender: Runnable?,
        task: Runnable,
        x: Float = 0f,
        y: Float = 0f,
        width: Float = -1f,
        height: Float = -1f,
        animProgress: Float,
        alphaProgress: Float,
        stencil: Boolean = false
    ) {
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager ?: return
        val sr = ScaledResolution(mc)
        val factor = sr.scaleFactor

        val resolvedW = if (width  < 0f) sr.scaledWidth.toFloat()  else width
        val resolvedH = if (height < 0f) sr.scaledHeight.toFloat() else height

        if (!GlobalAnimationSettings.enabled) {
            nvg.setupAndDraw(task)
            glRender?.run()
            return
        }

        ensureFramebuffer(nvg, mc.displayWidth, mc.displayHeight)

        val savedColor = snapshotClearColor()
        GlStateManager.enableTexture2D()
        GL11.glClearColor(0f, 0f, 0f, 0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        restoreClearColor(savedColor)

        val ctx = nvg.getContext()

        nvg.setupAndDraw(Runnable {
            nvg.resetScissor()
            task.run()
            nvg.resetScissor()
        })
        glRender?.run()

        mc.framebuffer.bindFramebuffer(true)

        nvg.setupAndDraw(Runnable {
            nvg.resetScissor()
            nvg.setAlpha(alphaProgress.coerceIn(0f, 1f))
            nvg.scale(
                x * factor, y * factor,
                resolvedW * factor, resolvedH * factor,
                animProgress
            )
            NanoVG.nvgBeginPath(ctx)
            if (stencil) {
                NanoVG.nvgRect(ctx, x * factor, y * factor, resolvedW * factor, resolvedH * factor)
            } else {
                NanoVG.nvgRect(ctx, 0f, 0f, mc.displayWidth.toFloat(), mc.displayHeight.toFloat())
            }
            NanoVG.nvgFillPaint(
                ctx,
                NanoVG.nvgImagePattern(ctx, 0f, 0f,
                    mc.displayWidth.toFloat(), mc.displayHeight.toFloat(),
                    0f, fb!!.image(), 1f, sharedPaint)
            )
            NanoVG.nvgFill(ctx)
            nvg.resetScissor()
        }, false)
    }


    fun wrap(task: Runnable, progress: Float) {
        wrap(null, task = task, animProgress = progress, alphaProgress = progress)
    }


    fun wrap(task: Runnable, animProgress: Float, alphaProgress: Float) {
        wrap(null, task = task, animProgress = animProgress, alphaProgress = alphaProgress)
    }

    override fun close() {
        releaseFramebuffer(Shindo.getInstance().nanoVGManager ?: return)
    }
}
