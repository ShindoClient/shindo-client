package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.TimerUtils
import java.awt.Color

class CompMainMenuTextBox : CompTextBoxBase {
    private val timer = TimerUtils()
    private val animation = SimpleAnimation()

    private var backgroundColor: Color = Color.WHITE
    private var fontColor: Color = Color.WHITE

    private var title: String? = null
    private var icon: String? = null

    private var passwordMode: Boolean = false

    constructor(x: Float, y: Float, width: Float, height: Float) : super(x, y, width, height)

    constructor() : super(0f, 0f, 0f, 0f)

    fun getBackgroundColor(): Color = backgroundColor
    fun getFontColor(): Color = fontColor

    fun setBackgroundColor(color: Color) {
        this.backgroundColor = color
    }

    fun setFontColor(color: Color) {
        this.fontColor = color
    }

    fun setPasswordMode(passwordMode: Boolean) {
        this.passwordMode = passwordMode
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
        val rawText = this.getText()
        val drawText = if (passwordMode) repeat(rawText.length) else rawText
        val focused = this.isFocused()

        var addX = 0f
        val halfHeight = height / 2f

        var outTextSize = 0
        var resultText = ""

        for (c in drawText.toCharArray()) {
            resultText += c

            if (nvgInstance.getTextWidth(resultText, halfHeight, Fonts.REGULAR) + halfHeight + 5 > getWidth()) {
                outTextSize++

                addX = getWidth() - nvgInstance.getTextWidth(resultText, halfHeight, Fonts.REGULAR) - halfHeight - 5
            }
        }

        if (selectionEnd < outTextSize) {
            val reversedText = StringBuilder(drawText).reverse().toString()

            addX =
                getWidth() - nvgInstance.getTextWidth(
                    reversedText.substring(outTextSize - selectionEnd),
                    halfHeight,
                    Fonts.REGULAR
                ) - halfHeight - 5
        }

        nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 4f, backgroundColor)

        nvgInstance.save()
        nvgInstance.scissor(getX() + 1, getY(), getWidth() - 2, getHeight())

        addX += if (title != null && icon != null) 16 else 5

        if (cursorPosition != selectionEnd) {
            val start = minOf(selectionEnd, cursorPosition)
            val end = maxOf(selectionEnd, cursorPosition)

            val selectionWidth = nvgInstance.getTextWidth(drawText.substring(start, end), halfHeight, Fonts.REGULAR)
            val offset = nvgInstance.getTextWidth(drawText.substring(0, start), halfHeight, Fonts.REGULAR)

            if (selectionWidth != 0f) {
                nvgInstance.drawRect(
                    getX() + offset + addX - 1,
                    getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(drawText, halfHeight, Fonts.REGULAR) / 2),
                    selectionWidth,
                    nvgInstance.getTextHeight(drawText, halfHeight, Fonts.REGULAR),
                    Color(0, 135, 247)
                )
            }
        }

        animation.setAnimation(if (!focused && rawText.isEmpty()) 1.0f else 0.0f, 16.0)

        if (icon != null && title != null) {
            nvgInstance.drawText(
                icon!!,
                getX() + 5,
                getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(drawText, halfHeight, Fonts.REGULAR) / 2),
                fontColor,
                halfHeight,
                Fonts.LEGACYICON
            )

            if (rawText.isEmpty()) {
                nvgInstance.save()
                nvgInstance.translate(animation.value * 8 - 8, 0f)
                nvgInstance.drawText(
                    title!!,
                    getX() + 16,
                    getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(
                        drawText,
                        halfHeight,
                        Fonts.REGULAR
                    ) / 2) + 1,
                    ColorUtils.applyAlpha(fontColor, (animation.value * 255).toInt()),
                    halfHeight,
                    Fonts.REGULAR
                )
                nvgInstance.restore()
            }
        }

        nvgInstance.drawText(
            drawText,
            getX() + addX,
            getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(drawText, halfHeight, Fonts.REGULAR) / 2) + 1,
            fontColor,
            halfHeight,
            Fonts.REGULAR
        )

        if (timer.delay(600)) {
            val position = nvgInstance.getTextWidth(drawText.substring(0, cursorPosition), halfHeight, Fonts.REGULAR)

            if (focused && cursorPosition == selectionEnd) {
                nvgInstance.drawRect(
                    getX() + addX + position,
                    getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(drawText, halfHeight, Fonts.REGULAR) / 2),
                    0.7f,
                    10f,
                    fontColor
                )
            }

            if (timer.delay(1200)) {
                timer.reset()
            }
        }

        nvgInstance.restore()
        super.draw(mouseX, mouseY, partialTicks)
    }

    fun setEmptyText(icon: String, title: String) {
        this.icon = icon
        this.title = title
    }

    private fun repeat(count: Int): String {
        if (count <= 0) return ""
        val builder = StringBuilder(count)
        repeat(count) { builder.append('*') }
        return builder.toString()
    }
}
