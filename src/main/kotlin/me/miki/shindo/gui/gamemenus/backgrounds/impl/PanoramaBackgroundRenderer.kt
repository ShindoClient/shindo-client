package me.miki.shindo.gui.gamemenus.backgrounds.impl

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Project

open class PanoramaBackgroundRenderer : AbstractBackground() {
    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldrenderer: WorldRenderer = tessellator.worldRenderer
    private var panoramaTimer = 0
    private var animationSpeed = 10f
    private var backgroundTexture: ResourceLocation? = null
    private val currentMode = PanoramaMode.REACTIVE
    private var lastPitch = 0.0f
    private var lastYaw = 0.0f
    private var width = 0f
    private var height = 0f
    private var lastFrameTime = System.currentTimeMillis()
    lateinit var mc: Minecraft
    private var centerX = 0f
    var centerY = 0f

    private enum class PanoramaMode {
        UP_AND_DOWN, FLAT_SPIN, REACTIVE, STATIONARY
    }

    override fun init() {
        mc = Minecraft.getMinecraft()
        width = mc.displayWidth.toFloat()
        height = mc.displayHeight.toFloat()
        val viewportTexture = DynamicTexture(256, 256)
        backgroundTexture = mc.textureManager.getDynamicTextureLocation("background", viewportTexture)
        centerX = mc.displayWidth / 2.0f
        centerY = mc.displayHeight / 2.0f
        lastFrameTime = System.currentTimeMillis()
    }

    override fun draw(sr: ScaledResolution, instance: Shindo, nvg: NanoVGManager, partialTicks: Float) {
        drawGradientRect(
            0,
            0,
            mc.displayWidth,
            mc.displayHeight,
            -1,
            -1
        ) // for some reason the menu goes white without this
        GlStateManager.disableAlpha()
        renderSkybox(partialTicks)
        GlStateManager.enableAlpha()
    }

    /**
     * Called from the main game loop to update the screen.
     */
    override fun update(width: Float, height: Float) {
        this.width = width
        this.height = height
    }

    private fun drawPanorama(partialTicks: Float) {
        ++panoramaTimer
        GlStateManager.matrixMode(GL11.GL_PROJECTION)
        GlStateManager.pushMatrix()
        GlStateManager.loadIdentity()
        Project.gluPerspective(FOV, 1.0f, 0.05f, 10.0f)
        GlStateManager.matrixMode(GL11.GL_MODELVIEW)
        GlStateManager.pushMatrix()
        GlStateManager.loadIdentity()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f)
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.disableCull()
        GlStateManager.depthMask(false)
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        val renderPasses = 8
        for (currentPass in 0 until renderPasses * renderPasses) {
            GlStateManager.pushMatrix()
            val offX = ((currentPass % renderPasses).toFloat() / renderPasses.toFloat() - 0.5f) / 64.0f
            val offY = ((currentPass / renderPasses).toFloat() / renderPasses.toFloat() - 0.5f) / 64.0f
            GlStateManager.translate(offX, offY, 0.0f)
            applyPanoramaRotation(partialTicks, animationSpeed)
            for (side in 0..5) {
                GlStateManager.pushMatrix()
                val rot = SIDE_ROTATIONS[side]
                if (rot[0] != 0.0f) {
                    GlStateManager.rotate(rot[0], rot[1], rot[2], rot[3])
                }
                mc.textureManager.bindTexture(titlePanoramaPaths[side])
                worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
                val alpha = 255 / (currentPass + 1)
                worldrenderer.pos(-1.0, -1.0, 1.0).tex(0.0, 0.0).color(255, 255, 255, alpha).endVertex()
                worldrenderer.pos(1.0, -1.0, 1.0).tex(1.0, 0.0).color(255, 255, 255, alpha).endVertex()
                worldrenderer.pos(1.0, 1.0, 1.0).tex(1.0, 1.0).color(255, 255, 255, alpha).endVertex()
                worldrenderer.pos(-1.0, 1.0, 1.0).tex(0.0, 1.0).color(255, 255, 255, alpha).endVertex()
                tessellator.draw()
                GlStateManager.popMatrix()
            }
            GlStateManager.popMatrix()
            GlStateManager.colorMask(true, true, true, false)
        }
        worldrenderer.setTranslation(0.0, 0.0, 0.0)
        GlStateManager.colorMask(true, true, true, true)
        GlStateManager.matrixMode(GL11.GL_PROJECTION)
        GlStateManager.popMatrix()
        GlStateManager.matrixMode(GL11.GL_MODELVIEW)
        GlStateManager.popMatrix()
        GlStateManager.depthMask(true)
        GlStateManager.enableCull()
        GlStateManager.enableDepth()
    }

    /**
     * Calculates and applies the rotation based on the current mode.
     */
    private fun applyPanoramaRotation(partialTicks: Float, rotationSpeed: Float) {
        val time = (panoramaTimer.toFloat() + partialTicks) * rotationSpeed
        val pitch: Float
        val yaw: Float
        when (currentMode) {
            PanoramaMode.UP_AND_DOWN -> {
                pitch = MathHelper.sin(time / 400.0f) * 25.0f + 20.0f
                yaw = -(time * 0.1f)
            }
            PanoramaMode.FLAT_SPIN -> {
                pitch = 20.0f
                yaw = -(time * 0.1f)
            }
            PanoramaMode.STATIONARY -> {
                pitch = 20.0f
                yaw = 0.0f
            }
            PanoramaMode.REACTIVE -> {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - lastFrameTime) / 1000.0f
                lastFrameTime = currentTime
                lastYaw = lerp(lastYaw, -((Mouse.getX() - centerX) / centerX * 45.0f), 12f * deltaTime)
                lastPitch = lerp(lastPitch, -((Mouse.getY() - centerY) / centerY * 25.0f), 12f * deltaTime)
                yaw = lastYaw
                pitch = lastPitch
            }
            else -> {
                pitch = 0.0f
                yaw = 0.0f
            }
        }
        GlStateManager.rotate(pitch, 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(yaw, 0.0f, 1.0f, 0.0f)
    }

    /**
     * Rotate and blurs the skybox view in the main menu
     */
    private fun rotateAndBlurSkybox() {
        mc.textureManager.bindTexture(backgroundTexture)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 256, 256)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.colorMask(true, true, true, false)
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
        GlStateManager.disableAlpha()
        val blurPasses = 3
        for (currentPass in 0 until blurPasses) {
            val alpha = 1.0f / (currentPass + 1).toFloat()
            val blurOff = (currentPass - blurPasses / 2).toFloat() / 256.0f
            worldrenderer.pos(width.toDouble(), height.toDouble(), 0.0).tex((0.0f + blurOff).toDouble(), 1.0)
                .color(1.0f, 1.0f, 1.0f, alpha).endVertex()
            worldrenderer.pos(width.toDouble(), 0.0, 0.0).tex((1.0f + blurOff).toDouble(), 1.0)
                .color(1.0f, 1.0f, 1.0f, alpha).endVertex()
            worldrenderer.pos(0.0, 0.0, 0.0).tex((1.0f + blurOff).toDouble(), 0.0).color(1.0f, 1.0f, 1.0f, alpha)
                .endVertex()
            worldrenderer.pos(0.0, height.toDouble(), 0.0).tex((0.0f + blurOff).toDouble(), 0.0)
                .color(1.0f, 1.0f, 1.0f, alpha).endVertex()
        }
        tessellator.draw()
        GlStateManager.enableAlpha()
        GlStateManager.colorMask(true, true, true, true)
    }

    /**
     * Renders the skybox in the main menu
     */
    private fun renderSkybox(partialTicks: Float) {
        mc.framebuffer.unbindFramebuffer()
        GlStateManager.viewport(0, 0, 256, 256)
        drawPanorama(partialTicks)
        for (i in 0..6) {
            rotateAndBlurSkybox()
        }
        mc.framebuffer.bindFramebuffer(true)
        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight)
        val scale = if (width > height) FOV / width else FOV / height
        val uScale = height * scale / 256.0f
        val vScale = width * scale / 256.0f
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldrenderer.pos(0.0, height.toDouble(), 0.0).tex((0.5f - uScale).toDouble(), (0.5f + vScale).toDouble())
            .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex()
        worldrenderer.pos(width.toDouble(), height.toDouble(), 0.0)
            .tex((0.5f - uScale).toDouble(), (0.5f - vScale).toDouble()).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex()
        worldrenderer.pos(width.toDouble(), 0.0, 0.0).tex((0.5f + uScale).toDouble(), (0.5f - vScale).toDouble())
            .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex()
        worldrenderer.pos(0.0, 0.0, 0.0).tex((0.5f + uScale).toDouble(), (0.5f + vScale).toDouble())
            .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex()
        tessellator.draw()
    }

    private fun lerp(start: Float, end: Float, amount: Float): Float {
        return start + amount * (end - start)
    }

    private fun drawGradientRect(left: Int, top: Int, right: Int, bottom: Int, startColor: Int, endColor: Int) {
        val f = (startColor shr 24 and 255).toFloat() / 255.0f
        val f1 = (startColor shr 16 and 255).toFloat() / 255.0f
        val f2 = (startColor shr 8 and 255).toFloat() / 255.0f
        val f3 = (startColor and 255).toFloat() / 255.0f
        val f4 = (endColor shr 24 and 255).toFloat() / 255.0f
        val f5 = (endColor shr 16 and 255).toFloat() / 255.0f
        val f6 = (endColor shr 8 and 255).toFloat() / 255.0f
        val f7 = (endColor and 255).toFloat() / 255.0f
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.shadeModel(7425)
        val tessellator: Tessellator = Tessellator.getInstance()
        val worldrenderer: WorldRenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).color(f5, f6, f7, f4).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).color(f5, f6, f7, f4).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    companion object {
        private const val FOV = 120.0f
        private val titlePanoramaPaths: Array<ResourceLocation> = arrayOf<ResourceLocation>(
            ResourceLocation("textures/gui/title/background/panorama_0.png"),
            ResourceLocation("textures/gui/title/background/panorama_1.png"),
            ResourceLocation("textures/gui/title/background/panorama_2.png"),
            ResourceLocation("textures/gui/title/background/panorama_3.png"),
            ResourceLocation("textures/gui/title/background/panorama_4.png"),
            ResourceLocation("textures/gui/title/background/panorama_5.png")
        )
        private val SIDE_ROTATIONS = arrayOf(
            floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f),
            floatArrayOf(90.0f, 0.0f, 1.0f, 0.0f),
            floatArrayOf(180.0f, 0.0f, 1.0f, 0.0f),
            floatArrayOf(-90.0f, 0.0f, 1.0f, 0.0f),
            floatArrayOf(90.0f, 1.0f, 0.0f, 0.0f),
            floatArrayOf(-90.0f, 1.0f, 0.0f, 0.0f)
        )
    }
}