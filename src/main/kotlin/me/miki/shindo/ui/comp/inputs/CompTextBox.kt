package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.comp.style.CompControlVariant
import me.miki.shindo.ui.comp.style.CompStyleResolver
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.TimerUtils
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color

open class CompTextBox : CompTextBoxBase {

    private val timer = TimerUtils()
    private val hintAnimation = SimpleAnimation()
    private val hoverAnimation = SimpleAnimation()
    private val focusAnimation = SimpleAnimation()
    private val validationAnimation = SimpleAnimation()
    private var defaultText: String? = null
    private var validator: ((String) -> Boolean)? = null

    constructor(x: Float, y: Float, width: Float, height: Float) : super(x, y, width, height)

    constructor() : super(0f, 0f, 0f, 0f)

    fun getDefaultText(): String? = defaultText
    fun setDefaultText(defaultText: String) {
        this.defaultText = defaultText
    }

    fun setValidator(validator: ((String) -> Boolean)?): CompTextBox {
        this.validator = validator
        return this
    }

    fun clearValidator(): CompTextBox {
        validator = null
        return this
    }

    fun hasValidationError(): Boolean {
        val currentValidator = validator ?: return false
        return !currentValidator.invoke(getText())
    }

    override fun setPosition(x: Float, y: Float, width: Float, height: Float) {
        this.setX(x)
        this.setY(y)
        this.setWidth(width)
        this.setHeight(height)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val paletteColors = palette

        val height = this.getHeight()
        val selectionEnd = this.getSelectionEnd()
        val cursorPosition = this.getCursorPosition()
        val text = this.getText()
        val enabled = this.isEnabled()
        val focused = this.isFocused()
        val hovered = MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())
        val hasError = enabled && hasValidationError()
        val textInset = 6f
        val textPaddingEnd = 6f

        var addX = 0f
        val halfHeight = (height * 0.5f).coerceAtLeast(8f)
        val referenceText = if (text.isEmpty()) "A" else text
        val textHeight = nvgInstance.getTextHeight(referenceText, halfHeight, Fonts.REGULAR)
        val textY = getY() + (getHeight() / 2f) - (textHeight / 2f) + 0.5f

        var outTextSize = 0
        var resultText = ""

        for (c in text.toCharArray()) {
            resultText += c
            if (nvgInstance.getTextWidth(
                    resultText,
                    halfHeight,
                    Fonts.REGULAR
                ) + textInset + textPaddingEnd > getWidth()
            ) {
                outTextSize++
                addX = getWidth() - nvgInstance.getTextWidth(
                    resultText,
                    halfHeight,
                    Fonts.REGULAR
                ) - textInset - textPaddingEnd
            }
        }

        if (selectionEnd < outTextSize) {
            val reversedText = StringBuilder(text).reverse().toString()
            addX =
                getWidth() - nvgInstance.getTextWidth(
                    reversedText.substring(outTextSize - selectionEnd),
                    halfHeight,
                    Fonts.REGULAR
                ) - textInset - textPaddingEnd
        }

        hoverAnimation.setAnimation(if (hovered && enabled) 1.0f else 0.0f, 16.0)
        focusAnimation.setAnimation(if (focused && enabled) 1.0f else 0.0f, 16.0)
        validationAnimation.setAnimation(if (hasError) 1.0f else 0.0f, 16.0)

        val baseBackground = CompStyleResolver.resolveControlBase(CompControlVariant.SECONDARY, paletteColors, accent)
        val hoverBackground = CompStyleResolver.resolveControlHover(CompControlVariant.SECONDARY, paletteColors, accent)
        val surfaceColor = ColorUtils.interpolateColor(baseBackground, hoverBackground, hoverAnimation.value.toDouble())
        val focusTint = ColorUtils.applyAlpha(accent.getColor1(), 110)
        var backgroundColor =
            ColorUtils.interpolateColor(surfaceColor, focusTint, (focusAnimation.value * 0.18f).toDouble())
        if (!enabled) {
            backgroundColor = ColorUtils.applyAlpha(backgroundColor, 118)
        }

        val idleOutline = ColorUtils.applyAlpha(paletteColors.getFontColor(ColorType.NORMAL), 28)
        val hoverOutline = ColorUtils.applyAlpha(accent.getColor1(), 74)
        val focusOutline = ColorUtils.applyAlpha(accent.getColor1(), 154)
        val errorOutline = Color(227, 92, 92, 218)
        val mixedOutline = ColorUtils.interpolateColor(idleOutline, hoverOutline, hoverAnimation.value.toDouble())
        val focusMixedOutline = ColorUtils.interpolateColor(mixedOutline, focusOutline, focusAnimation.value.toDouble())
        var outlineColor =
            ColorUtils.interpolateColor(focusMixedOutline, errorOutline, validationAnimation.value.toDouble())
        if (!enabled) {
            outlineColor = ColorUtils.applyAlpha(outlineColor, 96)
        }

        nvgInstance.drawRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            6f,
            backgroundColor
        )
        nvgInstance.drawOutlineRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            6f,
            1f,
            outlineColor
        )

        nvgInstance.save()
        nvgInstance.scissor(getX() + 2, getY(), getWidth() - 4, getHeight())

        if (cursorPosition != selectionEnd) {
            val start = minOf(selectionEnd, cursorPosition)
            val end = maxOf(selectionEnd, cursorPosition)

            val selectionWidth = nvgInstance.getTextWidth(text.substring(start, end), halfHeight, Fonts.REGULAR)
            val offset = nvgInstance.getTextWidth(text.substring(0, start), halfHeight, Fonts.REGULAR)

            if (selectionWidth != 0f) {
                nvgInstance.drawRect(
                    getX() + textInset + offset + addX,
                    textY - 0.5f,
                    selectionWidth,
                    textHeight + 1f,
                    ColorUtils.applyAlpha(accent.getColor1(), if (enabled) 160 else 94)
                )
            }
        }

        hintAnimation.setAnimation(if (!focused && text.isEmpty()) 1.0f else 0.0f, 16.0)

        if (text.isEmpty() && defaultText != null) {
            nvgInstance.save()
            nvgInstance.translate(hintAnimation.value * 6f - 6f, 0f)
            val hintAlpha = (hintAnimation.value * if (enabled) 200f else 140f).toInt().coerceIn(0, 255)
            nvgInstance.drawText(
                defaultText!!,
                getX() + textInset,
                textY,
                paletteColors.getFontColor(ColorType.NORMAL, hintAlpha),
                halfHeight,
                Fonts.REGULAR
            )
            nvgInstance.restore()
        }

        val textColor = if (enabled) {
            paletteColors.getFontColor(ColorType.DARK)
        } else {
            paletteColors.getFontColor(ColorType.NORMAL, 145)
        }
        nvgInstance.drawText(
            text,
            getX() + textInset + addX,
            textY,
            textColor,
            halfHeight,
            Fonts.REGULAR
        )

        if (timer.delay(600)) {
            val position =
                nvgInstance.getTextWidth(text, halfHeight, Fonts.REGULAR) -
                        nvgInstance.getTextWidth(text.substring(cursorPosition), halfHeight, Fonts.REGULAR)

            if (enabled && focused && cursorPosition == selectionEnd) {
                nvgInstance.drawRect(
                    getX() + textInset + addX + position,
                    textY - 0.5f,
                    0.85f,
                    textHeight + 1.25f,
                    paletteColors.getFontColor(ColorType.DARK)
                )
            }

            if (timer.delay(1200)) {
                timer.reset()
            }
        }

        nvgInstance.restore()
        super.draw(mouseX, mouseY, partialTicks)
    }
}
