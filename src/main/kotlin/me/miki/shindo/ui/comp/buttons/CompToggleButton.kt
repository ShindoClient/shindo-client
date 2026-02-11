package me.miki.shindo.ui.comp.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.ui.comp.style.CompControlVariant
import me.miki.shindo.ui.comp.templates.CompControlTemplate
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.ui.animation.value.ColorAnimation
import me.miki.shindo.ui.animation.value.SimpleAnimation
import java.awt.Color

class CompToggleButton : CompControlTemplate {
    private val opacityAnimation = SimpleAnimation()
    private val toggleAnimation = SimpleAnimation()
    private val circleAnimation = ColorAnimation()

    private val setting: BooleanSetting
    private var scale: Float = 1.0f


    fun getSetting(): BooleanSetting = setting
    fun getScale(): Float = scale

    constructor(x: Float, y: Float, scale: Float, setting: BooleanSetting) : super(x, y) {
        this.setting = setting

        setScale(scale)
        setVariant(CompControlVariant.SECONDARY)
        initState()


    }

    constructor(setting: BooleanSetting) : super(0f, 0f) {
        this.setting = setting
        setScale(1.0f)
        setVariant(CompControlVariant.SECONDARY)
        initState()
    }

    private fun initState() {
        toggleAnimation.value = if (setting.isToggled()) 20.5f else 2.5f
        circleAnimation.setColor(
                if (setting.isToggled()) Color.WHITE else palette.getBackgroundColor(ColorType.DARK)
        )
    }

    fun setScale(scale: Float) {
        this.scale = scale
        super.setWidth(34F * scale)
        super.setHeight(16F * scale)
    }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val nvgInstance = nvg
        val accentColor = accent
        val palette = palette

        val x = getX()
        val y = getY()
        val width = getWidth()
        val height = getHeight()
        val circle = 11 * scale
        val toggled = setting.isToggled()

        opacityAnimation.setAnimation(if (toggled) 1.0f else 0.0f, 14.0)
        toggleAnimation.setAnimation(if (toggled) 20.5f else 2.5f, 14.0)

        nvgInstance.drawRoundedRect(x, y, width, height, 7 * scale, palette.getBackgroundColor(ColorType.NORMAL))
        nvgInstance.drawGradientRoundedRect(
                x,
                y,
                width,
                height,
                7.4f * scale,
                ColorUtils.applyAlpha(accentColor.getColor1(), (opacityAnimation.value * 255).toInt()),
                ColorUtils.applyAlpha(accentColor.getColor2(), (opacityAnimation.value * 255).toInt())
        )
        nvgInstance.drawRoundedRect(
                x + toggleAnimation.value * scale,
                y + 2.5f * scale,
                circle,
                circle,
                circle / 2,
                circleAnimation.getColor(if (toggled) Color.WHITE else palette.getBackgroundColor(ColorType.DARK), 16)
        )
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            setting.setToggled(!setting.isToggled())
        }
    }
}
