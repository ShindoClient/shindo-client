package me.miki.shindo.management.shader

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.utils.ColorUtils
import java.awt.Color
import java.io.File

object ShaderBackgroundRenderer {

    private fun clampColor(value: Int): Int = maxOf(0, minOf(255, value))
    private fun clampFloat(value: Float): Float = maxOf(0f, minOf(1f, value))

    @JvmStatic
    fun renderShaderBackground(nvg: NanoVGManager, shaderFile: File?, x: Float, y: Float, width: Float, height: Float) {
        val instance = Shindo.getInstance()
        val shaderManager = instance.shaderManager
        if (shaderManager != null) {
            var shaderId = shaderManager.loadShader(net.minecraft.util.ResourceLocation("shindo/shaders/menu.fsh"))
            if (shaderId == -1 && shaderFile != null && shaderFile.exists()) {
                shaderId = shaderManager.loadShader(shaderFile)
            }
            if (shaderId != -1) {
                nvg.save()
                org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS)
                org.lwjgl.opengl.GL11.glPushMatrix()
                shaderManager.renderShader(shaderId, x, y, width, height)
                org.lwjgl.opengl.GL11.glPopMatrix()
                org.lwjgl.opengl.GL11.glPopAttrib()
                nvg.restore()
                return
            }
        }
        renderFallbackBackground(nvg, shaderFile, x, y, width, height)
    }

    private fun renderFallbackBackground(
        nvg: NanoVGManager,
        shaderFile: File?,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val shaderName = (shaderFile?.name ?: "default").toLowerCase()
        val time = (System.currentTimeMillis() % 10000) / 1000f

        when {
            shaderName.contains("rainbow") || shaderName.contains("colorful") -> {
                val color1 = ColorUtils.getRainbow((time * 100).toInt(), 10.0, 255)
                val color2 = ColorUtils.getRainbow((time * 100 + 180).toInt(), 10.0, 255)
                nvg.drawGradientRoundedRect(x, y, width, height, 5f, color1, color2)
            }

            shaderName.contains("wave") || shaderName.contains("ocean") -> {
                val blue1 = clampColor((128 + 50 * kotlin.math.sin(time.toDouble())).toInt())
                val green1 = clampColor((64 + 30 * kotlin.math.cos(time.toDouble())).toInt())
                val color1 = Color(0, blue1, 255)
                val color2 = Color(0, green1, 200)
                nvg.drawGradientRoundedRect(x, y, width, height, 5f, color1, color2)
            }

            shaderName.contains("fire") || shaderName.contains("flame") -> {
                val green = clampColor((100 + 50 * kotlin.math.sin(time * 2)).toInt())
                val color1 = Color(255, green, 0)
                val color2 = Color(200, 50, 0)
                nvg.drawGradientRoundedRect(x, y, width, height, 5f, color1, color2)
            }

            else -> {
                val r1 = clampFloat((0.5 + 0.3 * kotlin.math.sin(time * 0.5)).toFloat())
                val g1 = clampFloat((0.5 + 0.3 * kotlin.math.sin((time * 0.7 + 2))).toFloat())
                val b1 = clampFloat((0.5 + 0.3 * kotlin.math.sin((time * 0.9 + 4))).toFloat())
                val r2 = clampFloat((0.3 + 0.4 * kotlin.math.cos((time * 0.6 + 1))).toFloat())
                val g2 = clampFloat((0.3 + 0.4 * kotlin.math.cos((time * 0.8 + 3))).toFloat())
                val b2 = clampFloat((0.3 + 0.4 * kotlin.math.cos((time * 1.0 + 5))).toFloat())
                val color1 = Color(r1, g1, b1)
                val color2 = Color(r2, g2, b2)
                nvg.drawGradientRoundedRect(x, y, width, height, 5f, color1, color2)
            }
        }
    }

    @JvmStatic
    fun renderShaderPreview(nvg: NanoVGManager, shaderFile: File?, x: Float, y: Float, width: Float, height: Float) {
        renderFallbackBackground(nvg, shaderFile, x, y, width, height)
        nvg.drawRoundedRect(x, y, width, height, 6f, Color(255, 255, 255, 30))
        nvg.drawOutlineRoundedRect(x, y, width, height, 6f, 1f, Color(255, 255, 255, 60))
    }
}
