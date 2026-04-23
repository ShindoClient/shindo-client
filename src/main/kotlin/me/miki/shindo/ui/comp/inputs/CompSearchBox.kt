package me.miki.shindo.ui.comp.inputs

import me.miki.extensions.ui.graphics.nanovg.translate
import me.miki.shindo.Shindo
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.utils.TimerUtils
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
        val instance = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager!!
        val colorManager: ColorManager = instance.colorManager
        val palette: ColorPalette = colorManager.getPalette()

        val height = getHeight()
        val selectionEnd = this.getSelectionEnd()
        val cursorPosition = this.getCursorPosition()
        val text = this.getText()
        val focused = this.isFocused()

        var addX = 0f
        val halfHeight = height / 2f

        var outTextSize = 0
        var resultText = ""

        for (c in this.getText().toCharArray()) {
            resultText += c.toString()

            if (nvg.getTextWidth(resultText, halfHeight, Fonts.REGULAR) + halfHeight + 5 > this.getWidth()) {
                outTextSize++

                addX = this.getWidth() - nvg.getTextWidth(resultText, halfHeight, Fonts.REGULAR) - halfHeight - 5
            }
        }

        if (selectionEnd < outTextSize) {
            val reversedText = StringBuilder(this.getText()).reverse()

            addX = this.getWidth() - nvg.getTextWidth(
                reversedText.toString().substring(outTextSize - selectionEnd),
                halfHeight,
                Fonts.REGULAR
            ) - halfHeight - 5
        }

        nvg.drawRoundedRect(
            this.getX(),
            this.getY(),
            this.getWidth(),
            this.getHeight(),
            6f,
            palette.getBackgroundColor(ColorType.DARK)
        )

        nvg.save()
        nvg.scissor(this.getX() + 1, this.getY(), this.getWidth() - 2, this.getHeight())

        if (cursorPosition != selectionEnd) {
            val start = if (selectionEnd > cursorPosition) cursorPosition else selectionEnd
            val end = if (selectionEnd > cursorPosition) selectionEnd else cursorPosition

            val selectionWidth = nvg.getTextWidth(this.getText().substring(start, end), halfHeight, Fonts.REGULAR)
            val offset = nvg.getTextWidth(this.getText().substring(0, start), halfHeight, Fonts.REGULAR)

            if (selectionWidth != 0f) {
                nvg.drawRect(
                    this.getX() + 15 + offset + addX,
                    this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2),
                    selectionWidth,
                    nvg.getTextHeight(text, halfHeight, Fonts.REGULAR),
                    Color(0, 135, 247)
                )
            }
        }

        searchAnimation.setAnimation(if (!focused && this.getText().isEmpty()) 1.0f else 0.0f, 16)

        nvg.drawText(
            LegacyIcon.SEARCH,
            this.getX() + 5,
            this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2),
            palette.getFontColor(ColorType.NORMAL),
            halfHeight,
            Fonts.LEGACYICON
        )

        if (this.getText().isEmpty()) {
            nvg.save()
            nvg.translate((searchAnimation.value * 8) - 8, 0)
            nvg.drawText(
                TranslateText.SEARCH.getText(),
                this.getX() + 16,
                this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2) + 1,
                palette.getFontColor(ColorType.NORMAL, (searchAnimation.value * 200).toInt()),
                halfHeight,
                Fonts.REGULAR
            )
            nvg.restore()
        }

        nvg.drawText(
            this.getText(),
            this.getX() + 16 + addX,
            this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2) + 1,
            palette.getFontColor(ColorType.NORMAL),
            halfHeight,
            Fonts.REGULAR
        )

        if (timer.delay(600)) {
            val position = nvg.getTextWidth(this.getText(), halfHeight, Fonts.REGULAR) - nvg.getTextWidth(
                this.getText().substring(cursorPosition), halfHeight, Fonts.REGULAR
            )

            if (focused && cursorPosition == selectionEnd) {
                nvg.drawRect(
                    this.getX() + 16 + addX + position,
                    this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2) - 0.5f, 0.7f, 10f, palette.getFontColor(ColorType.DARK)
                )
            }

            if (timer.delay(1200)) {
                timer.reset()
            }
        }

        nvg.restore()
    }
}
