package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.TimerUtils
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import java.awt.Color

class CompSearchBox : CompTextBoxBase {
    private val timer = TimerUtils()
    private val searchAnimation = SimpleAnimation()

    constructor(x: Float, y: Float, width: Float, height: Float) : super(x, y, width, height)

    constructor() : super(0f, 0f, 0f, 0f)

    override fun setPosition(x: Float, y: Float, width: Float, height: Float) {
        this.setX(x)
        this.setY(y)
        this.setWidth(width)
        this.setHeight(height)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val paletteColors = palette

        val height = getHeight()
        val selectionEnd = this.getSelectionEnd()
        val cursorPosition = this.getCursorPosition()
        val text = this.getText()
        val focused = this.isFocused()

        var addX = 0f
        val halfHeight = height / 2f

        var outTextSize = 0
        var resultText = ""

        for (c in text.toCharArray()) {
            resultText += c

            if (nvgInstance.getTextWidth(resultText, halfHeight, Fonts.REGULAR) + halfHeight + 5 > getWidth()) {
                outTextSize++

                addX = getWidth() - nvgInstance.getTextWidth(resultText, halfHeight, Fonts.REGULAR) - halfHeight - 5
            }
        }

        if (selectionEnd < outTextSize) {
            val reversedText = StringBuilder(text).reverse().toString()

            addX =
                getWidth() - nvgInstance.getTextWidth(
                    reversedText.substring(outTextSize - selectionEnd),
                    halfHeight,
                    Fonts.REGULAR
                ) - halfHeight - 5
        }

        nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 6f, paletteColors.getBackgroundColor(ColorType.DARK))

        nvgInstance.save()
        nvgInstance.scissor(getX() + 1, getY(), getWidth() - 2, getHeight())

        if (cursorPosition != selectionEnd) {
            val start = minOf(selectionEnd, cursorPosition)
            val end = maxOf(selectionEnd, cursorPosition)

            val selectionWidth = nvgInstance.getTextWidth(text.substring(start, end), halfHeight, Fonts.REGULAR)
            val offset = nvgInstance.getTextWidth(text.substring(0, start), halfHeight, Fonts.REGULAR)

            if (selectionWidth != 0f) {
                nvgInstance.drawRect(
                    getX() + 15 + offset + addX,
                    getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2),
                    selectionWidth,
                    nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR),
                    Color(0, 135, 247)
                )
            }
        }

        searchAnimation.setAnimation(if (!focused && text.isEmpty()) 1.0f else 0.0f, 16.0)

        nvgInstance.drawText(
            LegacyIcon.SEARCH,
            getX() + 5,
            getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2),
            paletteColors.getFontColor(ColorType.NORMAL),
            halfHeight,
            Fonts.LEGACYICON
        )

        if (text.isEmpty()) {
            nvgInstance.save()
            nvgInstance.translate(searchAnimation.value * 8 - 8, 0f)
            nvgInstance.drawText(
                TranslateText.SEARCH.text,
                getX() + 16,
                getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2) + 1,
                paletteColors.getFontColor(ColorType.NORMAL, (searchAnimation.value * 200).toInt()),
                halfHeight,
                Fonts.REGULAR
            )
            nvgInstance.restore()
        }

        nvgInstance.drawText(
            text,
            getX() + 16 + addX,
            getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2) + 1,
            paletteColors.getFontColor(ColorType.NORMAL),
            halfHeight,
            Fonts.REGULAR
        )

        if (timer.delay(600)) {
            val position =
                nvgInstance.getTextWidth(text, halfHeight, Fonts.REGULAR) -
                    nvgInstance.getTextWidth(text.substring(cursorPosition), halfHeight, Fonts.REGULAR)

            if (focused && cursorPosition == selectionEnd) {
                nvgInstance.drawRect(
                    getX() + 16 + addX + position,
                    getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2) - 0.5f,
                    0.7f,
                    10f,
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
