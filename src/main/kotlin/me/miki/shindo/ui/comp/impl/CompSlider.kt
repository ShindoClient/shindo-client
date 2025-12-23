package me.miki.shindo.ui.comp.impl

import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.MathUtils
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import kotlin.math.roundToInt

class CompSlider : Comp {
    private val setting: NumberSetting
    private val animation = SimpleAnimation()
    private val draggingAnimation = SimpleAnimation()
    private var dragging = false
    
    private var circle: Boolean = true
    private var showValue: Boolean = true
    
    constructor(x: Float, y: Float, setting: NumberSetting, width: Float) : super(x, y) {
        this.setting = setting;
        setWidth(width);
        setHeight(4F);
    }

    constructor(setting: NumberSetting) : super(0f, 0f) {
        this.setting = setting;
        setWidth(90F);
        setHeight(4F);
    }

    fun getSetting(): NumberSetting { return setting }

    fun setCircle(circle: Boolean) { this.circle = circle }
    fun setShowValue(showValue: Boolean) { this.showValue = showValue }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val accentColor = accent
        val palette = palette

        val maxValue = setting.getMaxValue()
        val minValue = setting.getMinValue()
        val value = setting.getValue()

        val trackWidth = getWidth()
        val trackHeight = getHeight()
        val valueWidth = trackWidth * (value - minValue) / (maxValue - minValue)

        val diff = kotlin.math.min(trackWidth.toDouble(), kotlin.math.max(0.0, mouseX - (getX() - 1.5f).toDouble()))

        if (dragging) {
            if (diff == 0.0) {
                setting.setValue(minValue)
            } else {
                var newValue = diff / trackWidth * (maxValue - minValue) + minValue
                val step = setting.getStep()
                if (step > 0) {
                    newValue = (newValue / step).roundToInt() * step
                }
                setting.setValue(MathUtils.roundToPlace(newValue, if (setting.isInteger()) 0 else 2))
            }
        }

        animation.setAnimation(valueWidth.toFloat(), 16.0)
        val hovered = MouseUtils.isInside(
            mouseX,
            mouseY,
            getX() - 6,
            getY() - 3,
            trackWidth + 12,
            trackHeight * trackHeight
        )
        draggingAnimation.setAnimation(if (hovered) 1.0f else 0.0f, 16.0)

        nvgInstance.drawRoundedRect(getX(), getY(), trackWidth, trackHeight, 2f, palette.getBackgroundColor(me.miki.shindo.management.color.palette.ColorType.NORMAL))
        nvgInstance.drawGradientRoundedRect(
            getX(),
            getY(),
            animation.value,
            trackHeight,
            2f,
            accentColor.color1,
            accentColor.color2
        )

        if (circle) {
            nvgInstance.drawGradientRoundedRect(
                getX() + animation.value - 6,
                getY() - 2,
                8f,
                8f,
                4f,
                accentColor.color1,
                accentColor.color2
            )
        }

        if (showValue) {
            nvgInstance.save()
            nvgInstance.translate(0f, 2 - draggingAnimation.value * 2)

            val display = if (setting.isInteger()) {
                setting.getValue().toInt().toString()
            } else {
                String.format("%.2f", setting.getValue())
            }
            nvgInstance.drawText(
                display,
                getX() + animation.value - nvgInstance.getTextWidth(display, 7f, Fonts.REGULAR) / 2,
                getY() - 10,
                palette.getFontColor(
                    me.miki.shindo.management.color.palette.ColorType.NORMAL,
                    (draggingAnimation.value * 255).toInt()
                ),
                7f,
                Fonts.REGULAR
            )

            nvgInstance.restore()
        }

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val trackWidth = getWidth()
        val trackHeight = getHeight()
        if (MouseUtils.isInside(mouseX, mouseY, getX() - 6, getY() - 3, trackWidth + 12, trackHeight * trackHeight) && mouseButton == 0) {
            dragging = true
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        dragging = false
        super.mouseReleased(mouseX, mouseY, mouseButton)
    }
}
