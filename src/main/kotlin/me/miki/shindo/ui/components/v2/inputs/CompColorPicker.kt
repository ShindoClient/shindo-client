package me.miki.shindo.ui.components.v2.inputs

import me.miki.shindo.management.settings.impl.ColorSetting
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.ui.components.v2.Component
import me.miki.shindo.utils.MathUtils
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.util.ResourceLocation
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class CompColorPicker : Component {
    private val openAnimation = SimpleAnimation()
    private val hueAnimation = SimpleAnimation()
    private val saturationAnimation = SimpleAnimation()
    private val brightnessAnimation = SimpleAnimation()
    private val alphaAnimation = SimpleAnimation()

    private val colorSetting: ColorSetting
    private var isOpen: Boolean = false
    private var scale: Float = 1.0f

    private var hueDragging = false
    private var sbDragging = false
    private var alphaDragging = false

    constructor(x: Float, y: Float, setting: ColorSetting) : super(x, y) {
        this.colorSetting = setting
        scale = 1.0f
        this.isOpen = false
    }

    constructor(setting: ColorSetting) : super(0f, 0f) {
        this.colorSetting = setting
        scale = 1.0f
        this.isOpen = false
    }

    fun isOpen(): Boolean = isOpen
    fun getScale(): Float = scale

    fun setOpen(isOpen: Boolean) {
        this.isOpen = isOpen
    }

    fun setScale(scale: Float) {
        this.scale = scale
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg

        openAnimation.setAnimation(if (isOpen) 1.1f else 0.0f, 16.0)

        nvgInstance.save()
        nvgInstance.translate(0f, 26 * scale)

        val hueMaxValue = 1.0f
        val hueMinValue = 0.0f
        val hueValue = colorSetting.getHue()
        val size = 100 * scale

        nvgInstance.scale(getX(), getY(), size, 0f, max(0f, openAnimation.getValue() - 0.1f))

        val hueValueHeight = (size - 12 * scale) * (hueValue - hueMinValue) / (hueMaxValue - hueMinValue)
        val hueDiff = min(size.toDouble(), max(0.0, mouseY - (getY() + 28 * scale).toDouble()))

        hueAnimation.setAnimation(hueValueHeight, 16.0)

        if (hueDragging) {
            if (hueDiff == 0.0) {
                colorSetting.setHue(hueMinValue)
            } else {
                colorSetting.setHue(
                    MathUtils.roundToPlace(
                        (hueDiff / size) * (hueMaxValue - hueMinValue) + hueMinValue,
                        2
                    ).toFloat()
                )
            }
        }

        val sbMaxValue = 1.0f
        val sbMinValue = 0.0f
        val saturationValue = colorSetting.getSaturation()
        val brightnessValue = colorSetting.getBrightness()

        val saturationValueSize = (size - 12 * scale) * (saturationValue - sbMinValue) / (sbMaxValue - sbMinValue)
        val brightnessValueSize = (size - 12 * scale) * (brightnessValue - sbMinValue) / (sbMaxValue - sbMinValue)

        val brightnessDiff = min(size.toDouble(), max(0.0, (getY() + 30 * scale + size - mouseY).toDouble()))
        val saturationDiff = min(size.toDouble(), max(0.0, mouseX - (getX() - 4 * scale).toDouble()))

        brightnessAnimation.setAnimation(brightnessValueSize, 20.0)
        saturationAnimation.setAnimation(saturationValueSize, 20.0)

        if (sbDragging) {
            colorSetting.setBrightness(
                if (brightnessDiff == 0.0) sbMinValue
                else MathUtils.roundToPlace((brightnessDiff / size) * (sbMaxValue - sbMinValue) + sbMinValue, 2)
                    .toFloat()
            )

            colorSetting.setSaturation(
                if (saturationDiff == 0.0) sbMinValue
                else MathUtils.roundToPlace((saturationDiff / size) * (sbMaxValue - sbMinValue) + sbMinValue, 2)
                    .toFloat()
            )
        }

        val alphaMaxValue = 255
        val alphaMinValue = 0
        val alphaValue = colorSetting.getAlpha()
        val alphaWidth = size + 18 * scale

        val alphaDiff = min(alphaWidth.toDouble(), max(0.0, mouseX - (getX() - 4 * scale).toDouble()))

        val alphaValueSize = (alphaWidth - 12 * scale) * (alphaValue - alphaMinValue) / (alphaMaxValue - alphaMinValue)

        alphaAnimation.setAnimation(alphaValueSize, 20.0)

        if (alphaDragging) {
            if (colorSetting.isShowAlpha()) {
                colorSetting.setAlpha(
                    if (alphaDiff == 0.0) 0
                    else MathUtils.roundToPlace(
                        (alphaDiff / alphaWidth) * (alphaMaxValue - alphaMinValue) + alphaMinValue,
                        2
                    ).toInt()
                )
            } else {
                colorSetting.setAlpha(255)
            }
        }

        nvgInstance.drawHSBBox(getX(), getY(), size, size, 6f * scale, Color.getHSBColor(colorSetting.getHue(), 1f, 1f))
        nvgInstance.drawRoundedImage(
            ResourceLocation("shindo/hue.png"),
            getX() + 106 * scale,
            getY(),
            12 * scale,
            size,
            3 * scale
        )
        nvgInstance.drawArc(
            getX() + 112 * scale,
            getY() + hueAnimation.getValue() + 6 * scale,
            3 * scale,
            0f,
            360f,
            1.2f * scale,
            Color.WHITE
        )
        nvgInstance.drawArc(
            getX() + saturationAnimation.getValue() + 6 * scale,
            getY() + size - brightnessAnimation.getValue() - 6 * scale,
            3 * scale,
            0f,
            360f,
            1.2f * scale,
            Color.WHITE
        )

        if (colorSetting.isShowAlpha()) {
            nvgInstance.drawRoundedImage(
                ResourceLocation("shindo/alpha.png"),
                getX(),
                getY() + 106 * scale,
                size + 18 * scale,
                12 * scale,
                3 * scale
            )
            nvgInstance.drawAlphaBar(
                getX(),
                getY() + 106 * scale,
                alphaWidth,
                12 * scale,
                3 * scale,
                Color.getHSBColor(colorSetting.getHue(), 1f, 1f)
            )
            nvgInstance.drawArc(
                getX() + alphaAnimation.getValue() + 6 * scale,
                getY() + 112 * scale,
                3 * scale,
                0f,
                360f,
                1.2f * scale,
                Color.WHITE
            )
        }

        nvgInstance.restore()

        nvgInstance.drawRoundedRect(getX() + 106 * scale, getY(), 16 * scale, 16 * scale, 4f, colorSetting.getColor())

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isOpen) {
            val size = 100 * scale
            val alphaWidth = size + 18 * scale
            val addY = 26 * scale

            if (mouseButton == 0) {
                if (MouseUtils.isInside(mouseX, mouseY, getX() + 106 * scale, getY() + addY, 12 * scale, size)) {
                    hueDragging = true
                }

                if (MouseUtils.isInside(mouseX, mouseY, getX(), getY() + addY, size, size)) {
                    sbDragging = true
                }

                if (MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        getX(),
                        getY() + 106 * scale + addY,
                        alphaWidth,
                        12 * scale
                    ) && colorSetting.isShowAlpha()
                ) {
                    alphaDragging = true
                }
            }
        }

        if (mouseButton == 0 && isInsideOpen(mouseX, mouseY)) {
            isOpen = !isOpen
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        hueDragging = false
        sbDragging = false
        alphaDragging = false
        super.mouseReleased(mouseX, mouseY, mouseButton)
    }

    fun isShowAlpha(): Boolean = colorSetting.isShowAlpha()

    fun isInsideOpen(mouseX: Int, mouseY: Int): Boolean =
        MouseUtils.isInside(mouseX, mouseY, getX() + 106 * scale, getY(), 16 * scale, 16 * scale)
}