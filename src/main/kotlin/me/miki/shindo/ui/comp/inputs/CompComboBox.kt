package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.settings.impl.ComboSetting
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.ui.comp.style.CompControlVariant
import me.miki.shindo.ui.comp.style.CompStyleResolver
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import kotlin.math.abs

class CompComboBox : Comp {
    private val changeAnimation = SimpleAnimation()
    private val hoverAnimation = SimpleAnimation()
    private val leftHoverAnimation = SimpleAnimation()
    private val rightHoverAnimation = SimpleAnimation()
    private val leftPressAnimation = SimpleAnimation()
    private val rightPressAnimation = SimpleAnimation()

    private val setting: ComboSetting
    private var width: Float

    private var changeDirection: Int

    constructor(x: Float, y: Float, width: Float, setting: ComboSetting) : super(x, y) {
        this.width = width
        this.setting = setting
        this.changeDirection = 1
        this.changeAnimation.value = 1f
        setWidth(width)
        super.setHeight(CONTROL_HEIGHT)
    }

    constructor(width: Float, setting: ComboSetting) : super(0f, 0f) {
        this.width = width
        this.setting = setting
        this.changeDirection = 1
        this.changeAnimation.value = 1f
        setWidth(width)
        super.setHeight(CONTROL_HEIGHT)
    }

    override fun getWidth(): Float = width

    override fun setWidth(width: Float) {
        this.width = width
        super.setWidth(width)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val accentColor = accent
        val paletteColors = palette

        val x = getX()
        val y = getY()
        val controlWidth = width.coerceAtLeast(BUTTON_SIZE * 2f + 18f)
        val controlHeight = CONTROL_HEIGHT
        val leftX = x
        val rightX = x + controlWidth - BUTTON_SIZE
        val leftHovered = MouseUtils.isInside(mouseX, mouseY, leftX, y, BUTTON_SIZE, controlHeight)
        val rightHovered = MouseUtils.isInside(mouseX, mouseY, rightX, y, BUTTON_SIZE, controlHeight)
        val containerHovered = MouseUtils.isInside(mouseX, mouseY, x, y, controlWidth, controlHeight)

        changeAnimation.setAnimation(changeDirection.toFloat(), 16.0)
        hoverAnimation.setAnimation(if (containerHovered) 1.0f else 0.0f, 14.0)
        leftHoverAnimation.setAnimation(if (leftHovered) 1.0f else 0.0f, 14.0)
        rightHoverAnimation.setAnimation(if (rightHovered) 1.0f else 0.0f, 14.0)
        leftPressAnimation.setAnimation(
            if (leftPressAnimation.value > 0.08f) leftPressAnimation.value * 0.84f else 0.0f,
            16.0
        )
        rightPressAnimation.setAnimation(
            if (rightPressAnimation.value > 0.08f) rightPressAnimation.value * 0.84f else 0.0f,
            16.0
        )

        val baseBg = CompStyleResolver.resolveControlBase(CompControlVariant.SECONDARY, paletteColors, accentColor)
        val hoverBg = CompStyleResolver.resolveControlHover(CompControlVariant.SECONDARY, paletteColors, accentColor)
        var containerColor = ColorUtils.interpolateColor(baseBg, hoverBg, hoverAnimation.value.toDouble())
        val pressMix = (leftPressAnimation.value + rightPressAnimation.value).coerceIn(0f, 1f)
        if (pressMix > 0.08f) {
            containerColor = ColorUtils.darken(containerColor, pressMix * 0.15f)
        }

        nvgInstance.drawRoundedRect(x, y, controlWidth, controlHeight, 5f, containerColor)
        nvgInstance.drawOutlineRoundedRect(
            x,
            y,
            controlWidth,
            controlHeight,
            5f,
            1f,
            ColorUtils.applyAlpha(
                paletteColors.getFontColor(ColorType.NORMAL),
                (74 + hoverAnimation.value * 68f).toInt()
            )
        )

        drawSideButton(
            x = leftX,
            y = y,
            hoverValue = leftHoverAnimation.value,
            pressValue = leftPressAnimation.value,
            label = "<"
        )
        drawSideButton(
            x = rightX,
            y = y,
            hoverValue = rightHoverAnimation.value,
            pressValue = rightPressAnimation.value,
            label = ">"
        )

        val selectedText = setting.getOption()?.name ?: "-"
        val textAreaWidth = (controlWidth - BUTTON_SIZE * 2f - 8f).coerceAtLeast(18f)
        val clampedText = nvgInstance.getLimitText(selectedText, 8f, Fonts.REGULAR, textAreaWidth)
        val textAlpha = abs((changeAnimation.value * 255f).toDouble()).toInt().coerceIn(0, 255)
        val textX = x + controlWidth / 2f + (changeDirection - changeAnimation.value) * 14f
        val textHeight = nvgInstance.getTextHeight(clampedText, 8f, Fonts.REGULAR)

        nvgInstance.drawCenteredText(
            clampedText,
            textX,
            y + controlHeight / 2f - textHeight / 2f,
            paletteColors.getFontColor(ColorType.DARK, textAlpha),
            8f,
            Fonts.REGULAR
        )

        super.draw(mouseX, mouseY, partialTicks)
    }

    private fun drawSideButton(x: Float, y: Float, hoverValue: Float, pressValue: Float, label: String) {
        val nvgInstance = nvg
        val paletteColors = palette
        val accentColor = accent

        var bg = ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.DARK), 92)
        if (hoverValue > 0.01f) {
            val hoverBg = ColorUtils.applyAlpha(accentColor.getColor1(), 84)
            bg = ColorUtils.interpolateColor(bg, hoverBg, hoverValue.toDouble())
        }
        if (pressValue > 0.08f) {
            bg = ColorUtils.darken(bg, pressValue * 0.14f)
        }

        nvgInstance.drawRoundedRect(x + 1f, y + 1f, BUTTON_SIZE - 2f, CONTROL_HEIGHT - 2f, 4f, bg)
        nvgInstance.drawText(
            label,
            x + BUTTON_SIZE / 2f - nvgInstance.getTextWidth(label, 9f, Fonts.REGULAR) / 2f,
            y + CONTROL_HEIGHT / 2f - nvgInstance.getTextHeight(label, 9f, Fonts.REGULAR) / 2f,
            paletteColors.getFontColor(ColorType.DARK),
            9f,
            Fonts.REGULAR
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        val options = setting.getOptions()
        val max = options.size
        if (max <= 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        val x = getX()
        val y = getY()
        val controlWidth = width.coerceAtLeast(BUTTON_SIZE * 2f + 18f)
        val currentIndex = options.indexOf(setting.getOption()).let { if (it >= 0) it else 0 }

        if (MouseUtils.isInside(mouseX, mouseY, x, y, BUTTON_SIZE, CONTROL_HEIGHT)) {
            changeAnimation.value = 0f
            changeDirection = 1
            leftPressAnimation.value = 1f
            val nextIndex = if (currentIndex > 0) currentIndex - 1 else max - 1
            setting.setOption(options[nextIndex])
        } else if (MouseUtils.isInside(
                mouseX,
                mouseY,
                x + controlWidth - BUTTON_SIZE,
                y,
                BUTTON_SIZE,
                CONTROL_HEIGHT
            )
        ) {
            changeAnimation.value = 0f
            changeDirection = -1
            rightPressAnimation.value = 1f
            val nextIndex = if (currentIndex < max - 1) currentIndex + 1 else 0
            setting.setOption(options[nextIndex])
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    companion object {
        private const val CONTROL_HEIGHT = 16f
        private const val BUTTON_SIZE = 16f
    }
}
