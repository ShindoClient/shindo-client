package me.miki.shindo.ui.comp.impl

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.animation.ColorAnimation
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color

class CompToggleButton : Comp {
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
        initState()
        
        
    }

    constructor(setting: BooleanSetting) : super(0f, 0f) {
        this.setting = setting
        setScale(1.0f)
        initState()
    }

    private fun initState() {
        toggleAnimation.value = if (setting.isToggled()) 20.5f else 2.5f
        circleAnimation.setColor(
            if (setting.isToggled()) Color.WHITE else palette.getBackgroundColor(ColorType.DARK)
        )
    }

    fun setScale(scale: Float) {
        this.scale = scale;
        super.setWidth(34F * scale);
        super.setHeight(16F * scale);
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
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
            ColorUtils.applyAlpha(accentColor.color1, (opacityAnimation.value * 255).toInt()),
            ColorUtils.applyAlpha(accentColor.color2, (opacityAnimation.value * 255).toInt())
        )
        nvgInstance.drawRoundedRect(
            x + toggleAnimation.value * scale,
            y + 2.5f * scale,
            circle,
            circle,
            circle / 2,
            circleAnimation.getColor(if (toggled) Color.WHITE else palette.getBackgroundColor(ColorType.DARK), 16)
        )

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val x = getX()
        val y = getY()
        val width = getWidth()
        val height = getHeight()

        if (MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && mouseButton == 0) {
            setting.setToggled(!setting.isToggled())
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
