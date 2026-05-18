package me.miki.shindo.utils.render

import me.miki.shindo.injection.interfaces.IMixinMinecraft
import me.miki.shindo.injection.interfaces.IMixinShaderGroup
import me.miki.shindo.logger.ShindoLogger
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Timer

object BlurUtils {
    private val mc: Minecraft = Minecraft.getMinecraft()
    private var blurShader: ShaderGroup? = null

    private var lastScale = 0f
    private var lastScaleWidth = 0f
    private var lastScaleHeight = 0f

    private fun reinitShader() {
        try {
            val buffer = Framebuffer(mc.displayWidth, mc.displayHeight, true)
            buffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f)
            blurShader =
                ShaderGroup(
                    mc.textureManager,
                    mc.resourceManager,
                    mc.framebuffer,
                    ResourceLocation("shaders/post/blurArea.json"),
                )
            blurShader?.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load blur shader", e)
            blurShader = null
        }
    }

    @JvmStatic
    fun drawBlurScreen(radius: Float) {
        val sr = ScaledResolution(mc)
        val factor = sr.scaleFactor.toFloat()
        val factor2 = sr.scaledWidth.toFloat()
        val factor3 = sr.scaledHeight.toFloat()
        val x = 0f
        val y = 0f
        val width = sr.scaledWidth.toFloat()
        val height = sr.scaledHeight.toFloat()

        if (blurShader == null || lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
            reinitShader()
        }

        val shader = blurShader ?: return

        lastScale = factor
        lastScaleWidth = factor2
        lastScaleHeight = factor3

        val mixinShader = shader as IMixinShaderGroup
        val shaders = mixinShader.listShaders

        shaders[0]
            .shaderManager
            .getShaderUniform("BlurXY")
            .set(x * (sr.scaleFactor / 2.0f), (factor3 - height) * (sr.scaleFactor / 2.0f))
        shaders[1]
            .shaderManager
            .getShaderUniform("BlurXY")
            .set(x * (sr.scaleFactor / 2.0f), (factor3 - height) * (sr.scaleFactor / 2.0f))
        shaders[0]
            .shaderManager
            .getShaderUniform("BlurCoord")
            .set((width - x) * (sr.scaleFactor / 2.0f), (height - y) * (sr.scaleFactor / 2.0f))
        shaders[1]
            .shaderManager
            .getShaderUniform("BlurCoord")
            .set((width - x) * (sr.scaleFactor / 2.0f), (height - y) * (sr.scaleFactor / 2.0f))
        shaders[0].shaderManager.getShaderUniform("Radius").set(radius)
        shaders[1].shaderManager.getShaderUniform("Radius").set(radius)

        shader.loadShaderGroup(((mc as IMixinMinecraft).timer as Timer).renderPartialTicks)
        mc.framebuffer.bindFramebuffer(true)
    }
}
