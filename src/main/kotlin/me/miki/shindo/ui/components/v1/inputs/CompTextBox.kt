package me.miki.shindo.ui.components.v1.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v1.value.SimpleAnimation
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.TimerUtils


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

        val height = this.getHeight()
        val selectionEnd = this.getSelectionEnd()
        val cursorPosition = this.getCursorPosition()
        val text = this.getText()
        val enabled = this.isEnabled()
        val focused = this.isFocused()
        val textInset = 6f
        val textPaddingEnd = 6f

        var addX = 0f
        val halfHeight = (height * 0.5f).coerceAtLeast(8f)
        val referenceText = text.ifEmpty { "A" }
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

        nvgInstance.drawRoundedRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 6f, palette.getBackgroundColor(ColorType.NORMAL));


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
            nvgInstance.translate((hintAnimation.value * 8f) - 8f, 0f)
            val hintAlpha = (hintAnimation.value * if (enabled) 200f else 140f).toInt().coerceIn(0, 255)
            nvgInstance.drawText(defaultText!!, this.getX() + 5, this.getY() + (this.getHeight() / 2) - (nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2) + 1, palette.getFontColor(ColorType.DARK,  hintAlpha), halfHeight, Fonts.REGULAR);
            nvgInstance.restore();
        }

        nvgInstance.drawText(this.getText(), this.getX() + 5 + addX, this.getY() + (this.getHeight() / 2) - (nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2) + 1, palette.getFontColor(ColorType.DARK), halfHeight, Fonts.REGULAR);


        if (timer.delay(600)) {
            val position = nvgInstance.getTextWidth(this.getText(), halfHeight, Fonts.REGULAR) - nvgInstance.getTextWidth(
                this.getText().substring(cursorPosition), halfHeight, Fonts.REGULAR
            )

            if (focused && cursorPosition == selectionEnd) {
                nvgInstance.drawRect(
                    this.getX() + 5 + addX + position,
                    this.getY() + (this.getHeight() / 2) - (nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2),
                    0.7f, 10f, palette.getFontColor(ColorType.DARK)
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
