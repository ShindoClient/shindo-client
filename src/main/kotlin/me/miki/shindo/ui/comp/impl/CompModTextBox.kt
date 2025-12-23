package me.miki.shindo.ui.comp.impl

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.settings.impl.TextSetting
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.impl.field.CompTextBoxBase
import me.miki.shindo.utils.TimerUtils
import java.awt.Color

class CompModTextBox : CompTextBoxBase {
    private val setting: TextSetting
    private val timer = TimerUtils()

    constructor(x: Float, y: Float, width: Float, height: Float, setting: TextSetting) : super(x, y, width, height) {
        this.setting = setting
        this.setText(setting.getText())
    }

    constructor(setting: TextSetting) : super(0f, 0f, 0f, 0f) {
        this.setting = setting
        this.setText(setting.getText())
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

        nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 4f, paletteColors.getBackgroundColor(ColorType.NORMAL))

        nvgInstance.save()
        nvgInstance.scissor(getX() + 1, getY(), getWidth() - 2, getHeight())

        if (cursorPosition != selectionEnd) {
            val start = minOf(selectionEnd, cursorPosition)
            val end = maxOf(selectionEnd, cursorPosition)

            val selectionWidth = nvgInstance.getTextWidth(text.substring(start, end), halfHeight, Fonts.REGULAR)
            val offset = nvgInstance.getTextWidth(text.substring(0, start), halfHeight, Fonts.REGULAR)

            if (selectionWidth != 0f) {
                nvgInstance.drawRect(
                    getX() + 4 + offset + addX,
                    getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2),
                    selectionWidth,
                    nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR),
                    Color(0, 135, 247)
                )
            }
        }

        nvgInstance.drawText(
            text,
            getX() + 5 + addX,
            getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2) + 1,
            paletteColors.getFontColor(ColorType.DARK),
            halfHeight,
            Fonts.REGULAR
        )

        if (timer.delay(600)) {
            val position =
                nvgInstance.getTextWidth(text, halfHeight, Fonts.REGULAR) -
                    nvgInstance.getTextWidth(text.substring(cursorPosition), halfHeight, Fonts.REGULAR)

            if (focused && cursorPosition == selectionEnd) {
                nvgInstance.drawRect(
                    getX() + 5 + addX + position,
                    getY() + (getHeight() / 2) - (nvgInstance.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2),
                    0.7f,
                    10f,
                    paletteColors.getFontColor(ColorType.DARK)
                )
            }

            if (timer.delay(1200)) {
                timer.reset()
            }
        }

        if (!focused) {
            setting.setText(this.getText())
        }

        nvgInstance.restore()
        super.draw(mouseX, mouseY, partialTicks)
    }
}
