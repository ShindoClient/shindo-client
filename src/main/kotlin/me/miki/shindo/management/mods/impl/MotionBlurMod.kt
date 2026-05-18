package me.miki.shindo.management.mods.impl

import me.miki.shindo.injection.interfaces.IMixinShaderGroup
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventShader
import me.miki.shindo.management.event.impl.EventUpdateDisplay
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.sound.Sound.Companion.play
import me.miki.shindo.management.sound.Sounds
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.shader.Shader
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.util.function.Consumer

@Suppress("unused", "UNUSED_PARAMETER")
class MotionBlurMod :
    Mod(
        TranslateText.MOTION_BLUR,
        TranslateText.MOTION_BLUR_DESCRIPTION,
        ModCategory.RENDER,
        LegacyIcon.MOD_MOTION_BLUR,
    ) {
    private val motion_blur = ResourceLocation("minecraft:shaders/post/motion_blur.json")

    @Property(type = PropertyType.COMBO, translate = TranslateText.TYPE)
    private val mode = Mode.SHADER

    @Property(type = PropertyType.NUMBER, translate = TranslateText.AMOUNT, min = 0.1, max = 0.9, current = 0.5)
    private val amountSetting = 0.5

    private var lastCheck = 0L
    private var group: ShaderGroup? = null
    private var groupBlur = 0f
    private var loaded = false
    private var prevWidth = 0
    private var prevHeight = 0

    override fun setup() {
        loaded = false
    }

    @EventTarget
    fun onShader(event: EventShader) {
        val sr = ScaledResolution(mc)

        if (mode == Mode.SHADER) {
            if (group == null || prevWidth != sr.scaledWidth || prevHeight != sr.scaledHeight) {
                prevWidth = sr.scaledWidth
                prevHeight = sr.scaledHeight

                groupBlur = amountSetting.toFloat()

                try {
                    group =
                        ShaderGroup(mc.textureManager, mc.resourceManager, mc.framebuffer, motion_blur)
                    group!!.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
                } catch (e: Exception) {
                    ShindoLogger.error("[MotionBlurMod] | Failed to load shader", e)
                }
            }

            if (groupBlur != amountSetting.toFloat() || !loaded) {
                loaded = true
                (group as IMixinShaderGroup).listShaders.forEach(
                    Consumer { shader: Shader? ->
                        shader!!.shaderManager.getShaderUniform("BlurFactor")?.set(amountSetting.toFloat())
                    },
                )
                groupBlur = amountSetting.toFloat()
            }

            event.groups.add(group!!)
        }
    }

    @EventTarget
    fun onUpdateDisplay(event: EventUpdateDisplay?) {
        if (mode == Mode.ACCUM) {
            if (group != null) {
                group = null
                loaded = false
            }

            if (mc.thePlayer != null) {
                GL11.glAccum(259, amountSetting.toFloat())
                GL11.glAccum(256, 1.0f - amountSetting.toFloat())
                GL11.glAccum(258, 1.0f)

                if (lastCheck + 1000L < System.currentTimeMillis()) {
                    lastCheck = System.currentTimeMillis()

                    val error = GL11.glGetError()

                    if (error == 1282) {
                        this.setToggled(false)
                        try {
                            play(Sounds.SHINDO_AUDIO_ERROR, false)
                        } catch (ignored: Exception) {
                        }
                    }
                }
            }
        }
    }

    override fun onEnable() {
        group = null
        super.onEnable()
    }

    private enum class Mode(
        private val translate: TranslateText,
    ) : PropertyEnum {
        ACCUM(TranslateText.ACCUM),
        SHADER(TranslateText.SHADER),
        ;

        override fun getTranslate(): TranslateText = translate
    }
}
