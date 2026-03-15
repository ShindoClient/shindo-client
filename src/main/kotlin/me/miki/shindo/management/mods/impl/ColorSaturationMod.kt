package me.miki.shindo.management.mods.impl

import com.google.gson.JsonSyntaxException
import me.miki.shindo.injection.interfaces.IMixinShaderGroup
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventShader
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.shader.Shader
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation
import java.io.IOException
import java.util.function.Consumer

class ColorSaturationMod : Mod(
    TranslateText.COLOR_SATURATION,
    TranslateText.COLOR_SATURATION_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_COLOR_SATURATION
) {
    private val colorsaturation = ResourceLocation("minecraft:shaders/post/colorsaturation.json")

    @Property(type = PropertyType.NUMBER, translate = TranslateText.HUE, min = 0.0, max = 1.0, current = 0.0)
    private val hueSetting = 0.0

    @Property(type = PropertyType.NUMBER, translate = TranslateText.BRIGHTNESS, min = 0.0, max = 1.0, current = 0.5)
    private val brightnessSetting = 0.5

    @Property(type = PropertyType.NUMBER, translate = TranslateText.CONTRAST, min = 0.0, max = 1.0, current = 0.5)
    private val contrastSetting = 0.5

    @Property(type = PropertyType.NUMBER, translate = TranslateText.SATURATION, min = 0.0, max = 1.0, current = 0.5)
    private val saturationSetting = 0.5
    private var group: ShaderGroup? = null
    private var prevHue = 0f
    private var prevSaturation = 0f
    private var prevBrightness = 0f
    private var prevContrast = 0f
    private var prevWidth = 0
    private var prevHeight = 0

    @EventTarget
    fun onShader(event: EventShader) {
        val sr = ScaledResolution(mc)

        val hue = hueSetting.toFloat()
        val saturation = saturationSetting.toFloat()
        val brightness = brightnessSetting.toFloat()
        val contrast = contrastSetting.toFloat()

        if (group == null || prevWidth != sr.scaledWidth || prevHeight != sr.scaledHeight) {
            prevWidth = sr.scaledWidth
            prevHeight = sr.scaledHeight

            prevHue = hue
            prevSaturation = saturation
            prevBrightness = brightness
            prevContrast = contrast

            try {
                group =
                    ShaderGroup(mc.textureManager, mc.resourceManager, mc.framebuffer, colorsaturation)
                group!!.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
            } catch (error: JsonSyntaxException) {
                error.printStackTrace()
            } catch (error: IOException) {
                error.printStackTrace()
            }
        }

        if (prevHue != hue || prevSaturation != saturation || prevBrightness != brightness || prevContrast != contrast) {
            (group as IMixinShaderGroup).getListShaders().forEach(Consumer { shader: Shader? ->
                val hueUniform = shader!!.shaderManager.getShaderUniform("hue")
                val contrastUniform = shader.shaderManager.getShaderUniform("Contrast")
                val brightnessUniform = shader.shaderManager.getShaderUniform("Brightness")
                val saturationUniform = shader.shaderManager.getShaderUniform("Saturation")

                if (hueUniform != null) {
                    hueUniform.set(hue)
                }

                if (contrastUniform != null) {
                    contrastUniform.set(contrast)
                }

                if (brightnessUniform != null) {
                    brightnessUniform.set(brightness)
                }
                if (saturationUniform != null) {
                    saturationUniform.set(saturation)
                }
            })

            prevHue = hue
            prevSaturation = saturation
            prevBrightness = brightness
            prevContrast = contrast
        }

        event.groups.add(group!!)
    }

    override fun onEnable() {
        super.onEnable()
        group = null
    }
}




