package me.miki.shindo.ui.comp.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.comp.style.CompControlVariant
import me.miki.shindo.ui.comp.style.CompStyleResolver
import me.miki.shindo.ui.comp.templates.CompControlTemplate
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

class CompToggleButton : CompControlTemplate {
    private val hoverAnimation = SimpleAnimation()
    private val pressAnimation = SimpleAnimation()
    private val toggleAnimation = SimpleAnimation()

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
        toggleAnimation.value = if (setting.isToggled()) 1.0f else 0.0f
    }

    fun setScale(scale: Float) {
        this.scale = scale
        super.setWidth(34f * scale)
        super.setHeight(16f * scale)
    }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val nvgInstance = nvg
        val accentColor = accent
        val palette = palette

        val x = getX()
        val y = getY()
        val width = getWidth()
        val height = getHeight()
        val enabled = isEnabled()
        val toggled = setting.isToggled()
        val radius = height / 2f
        val knobSize = (height - (4f * scale)).coerceAtLeast(8f * scale)
        val knobInset = (height - knobSize) / 2f
        val knobTravel = width - (knobInset * 2f) - knobSize

        hoverAnimation.setAnimation(if (hovered && enabled) 1.0f else 0.0f, 16.0)
        pressAnimation.setAnimation(if (pressAnimation.value > 0.08f) pressAnimation.value * 0.83f else 0.0f, 16.0)
        toggleAnimation.setAnimation(if (toggled) 1.0f else 0.0f, 16.0)

        val baseTrack = CompStyleResolver.resolveControlBase(CompControlVariant.SECONDARY, palette, accentColor)
        val hoverTrack = CompStyleResolver.resolveControlHover(CompControlVariant.SECONDARY, palette, accentColor)
        var trackColor = ColorUtils.interpolateColor(baseTrack, hoverTrack, hoverAnimation.value.toDouble())
        if (pressAnimation.value > 0.08f) {
            trackColor = ColorUtils.darken(trackColor, pressAnimation.value * 0.16f)
        }
        if (!enabled) {
            trackColor = ColorUtils.applyAlpha(trackColor, 116)
        }

        val activeGradientStart = ColorUtils.applyAlpha(
            accentColor.getColor1(),
            if (enabled) (80 + (toggleAnimation.value * 145f).toInt()) else 86
        )
        val activeGradientEnd = ColorUtils.applyAlpha(
            accentColor.getColor2(),
            if (enabled) (80 + (toggleAnimation.value * 145f).toInt()) else 86
        )

        val outlineIdle = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 34)
        val outlineHover = ColorUtils.applyAlpha(accentColor.getColor1(), 98)
        var outlineColor = ColorUtils.interpolateColor(outlineIdle, outlineHover, hoverAnimation.value.toDouble())
        if (!enabled) {
            outlineColor = ColorUtils.applyAlpha(outlineColor, 92)
        }

        nvgInstance.drawRoundedRect(x, y, width, height, radius, trackColor)
        nvgInstance.drawGradientRoundedRect(x, y, width, height, radius, activeGradientStart, activeGradientEnd)
        nvgInstance.drawOutlineRoundedRect(x, y, width, height, radius, 1f, outlineColor)

        val knobX = x + knobInset + knobTravel * toggleAnimation.value
        var knobColor = ColorUtils.interpolateColor(
            palette.getBackgroundColor(ColorType.DARK),
            Color.WHITE,
            toggleAnimation.value.toDouble()
        )
        if (hoverAnimation.value > 0.0f) {
            knobColor = ColorUtils.interpolateColor(knobColor, Color.WHITE, (hoverAnimation.value * 0.2f).toDouble())
        }
        if (!enabled) {
            knobColor = ColorUtils.applyAlpha(knobColor, 144)
        }

        nvgInstance.drawRoundedRect(
            knobX,
            y + knobInset,
            knobSize,
            knobSize,
            knobSize / 2f,
            knobColor
        )
        nvgInstance.drawOutlineRoundedRect(
            knobX,
            y + knobInset,
            knobSize,
            knobSize,
            knobSize / 2f,
            1f,
            ColorUtils.applyAlpha(Color.BLACK, if (enabled) 42 else 22)
        )
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && isEnabled()) {
            pressAnimation.value = 1.0f
            setting.setToggled(!setting.isToggled())
        }
    }
}
