package me.miki.shindo.gui

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.curve.DecelerateAnimation
import me.miki.shindo.utils.GlUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import java.awt.Color

class GuiSplashScreen {

    private val mc = Minecraft.getMinecraft()
    private var framebuffer: Framebuffer? = null
    private var fadeAnimation: Animation? = null

    fun draw() {
        framebuffer = GlUtils.createFrameBuffer(framebuffer)

        val sr = ScaledResolution(mc)
        val scaleFactor = sr.scaleFactor
        val nvg = NanoVGManager()

        if (fadeAnimation == null) {
            fadeAnimation = DecelerateAnimation(1000, 1.0)
            fadeAnimation!!.setDirection(Direction.FORWARDS)
            fadeAnimation!!.reset()
        }

        mc.updateDisplay()

        while (!fadeAnimation!!.isDone(Direction.FORWARDS)) {
            framebuffer!!.framebufferClear()
            framebuffer!!.bindFramebuffer(true)

            GlStateManager.matrixMode(GL11.GL_PROJECTION)
            GlStateManager.loadIdentity()
            GlStateManager.ortho(0.0, sr.scaledWidth.toDouble(), sr.scaledHeight.toDouble(), 0.0, 1000.0, 3000.0)
            GlStateManager.matrixMode(GL11.GL_MODELVIEW)
            GlStateManager.loadIdentity()
            GlStateManager.translate(0.0f, 0.0f, -2000.0f)
            GlStateManager.disableLighting()
            GlStateManager.disableFog()
            GlStateManager.disableDepth()
            GlStateManager.enableTexture2D()

            GlStateManager.color(0f, 0f, 0f, 0f)
            GlStateManager.enableBlend()
            GlStateManager.enableAlpha()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            nvg.setupAndDraw(Runnable {
                nvg.drawRect(0f, 0f, sr.scaledWidth.toFloat(), sr.scaledHeight.toFloat(), Color.BLACK)
                nvg.drawCenteredText(
                    LegacyIcon.SHINDO,
                    sr.scaledWidth / 2f,
                    (sr.scaledHeight / 2f) - (nvg.getTextHeight(LegacyIcon.SHINDO, 130f, Fonts.LEGACYICON) / 2) - 1,
                    Color(255, 255, 255, (fadeAnimation!!.getValue() * 255).toInt()),
                    130f,
                    Fonts.LEGACYICON
                )
            })

            framebuffer!!.unbindFramebuffer()
            framebuffer!!.framebufferRender(sr.scaledWidth * scaleFactor, sr.scaledHeight * scaleFactor)

            GlUtils.setAlphaLimit(1f)

            mc.updateDisplay()
        }

        Shindo.getInstance().nanoVGManager = nvg
    }
}
