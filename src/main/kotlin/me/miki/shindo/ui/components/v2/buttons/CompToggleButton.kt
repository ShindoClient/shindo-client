package me.miki.shindo.ui.components.v2.buttons

import me.miki.shindo.gui.modmenu.v2.style.ModMenuListCardStyle
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.ui.components.v2.templates.CompControlTemplate
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

class CompToggleButton : CompControlTemplate {
    private val toggleAnimation = SimpleAnimation()

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
        toggleAnimation.setValue(if (setting.isToggled()) 1.0f else 0.0f)
    }

    fun setScale(scale: Float) {
        this.scale = scale
        super.setWidth(34f * scale)
        super.setHeight(16f * scale)
    }

    override fun drawInteractive(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        hovered: Boolean,
    ) {
        val nvgInstance = nvg
        val accentColor = accent
        val palette = palette

        val x = getX()
        val y = getY()
        val width = getWidth()
        val height = getHeight()
        val toggled = setting.isToggled()

        toggleAnimation.setAnimation(if (toggled) 1.0f else 0.0f, 16.0)
        val progress = toggleAnimation.getValue()

        val toggleRadius = height / 2f
        val toggleBase =
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), ModMenuListCardStyle.TOGGLE_BASE_ALPHA)
        nvgInstance.drawRoundedRect(x, y, width, height, toggleRadius, toggleBase)

        if (progress > 0f) {
            nvgInstance.drawGradientRoundedRect(
                x,
                y,
                width,
                height,
                toggleRadius,
                ColorUtils.applyAlpha(accentColor.getColor1(), (progress * 255).toInt()),
                ColorUtils.applyAlpha(accentColor.getColor2(), (progress * 255).toInt()),
            )
        }

        val knobSize = height - 6
        val knobX = x + 3 + progress * (width - knobSize - 6)
        val knobY = y + 3
        nvgInstance.drawRoundedRect(knobX, knobY, knobSize, knobSize, knobSize / 2f, Color.WHITE)
    }

    override fun onMouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton == 0 && isEnabled()) {
            setting.setToggled(!setting.isToggled())
        }
    }
}
