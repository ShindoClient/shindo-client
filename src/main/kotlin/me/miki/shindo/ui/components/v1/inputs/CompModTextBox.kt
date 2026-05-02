package me.miki.shindo.ui.components.v1.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.settings.impl.TextSetting
import me.miki.shindo.ui.animation.v1.value.SimpleAnimation
import me.miki.shindo.ui.components.v1.style.CompControlVariant
import me.miki.shindo.ui.components.v1.style.CompStyleResolver
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.TimerUtils
import me.miki.shindo.utils.mouse.MouseUtils

class CompModTextBox : CompTextBoxBase {
    private val setting: TextSetting
    private val timer = TimerUtils()
    private val hoverAnimation = SimpleAnimation()
    private val focusAnimation = SimpleAnimation()

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
        val accentColor = accent

        val height = getHeight()
        val selectionEnd = this.getSelectionEnd()
        val cursorPosition = this.getCursorPosition()
        val text = this.getText()
        val enabled = this.isEnabled()
        val focused = this.isFocused()
        val hovered = MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())
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

        val baseBackground =
            CompStyleResolver.resolveControlBase(CompControlVariant.SECONDARY, paletteColors, accentColor)
        val hoverBackground =
            CompStyleResolver.resolveControlHover(CompControlVariant.SECONDARY, paletteColors, accentColor)
        val shellColor = ColorUtils.interpolateColor(baseBackground, hoverBackground, hoverAnimation.value.toDouble())
        val focusTint = ColorUtils.applyAlpha(accentColor.getColor1(), 106)
        var backgroundColor =
            ColorUtils.interpolateColor(shellColor, focusTint, (focusAnimation.value * 0.18f).toDouble())
        if (!enabled) {
            backgroundColor = ColorUtils.applyAlpha(backgroundColor, 116)
        }

        val idleOutline = ColorUtils.applyAlpha(paletteColors.getFontColor(ColorType.NORMAL), 28)
        val hoverOutline = ColorUtils.applyAlpha(accentColor.getColor1(), 76)
        val focusOutline = ColorUtils.applyAlpha(accentColor.getColor1(), 154)
        val mixedOutline = ColorUtils.interpolateColor(idleOutline, hoverOutline, hoverAnimation.value.toDouble())
        var outlineColor = ColorUtils.interpolateColor(mixedOutline, focusOutline, focusAnimation.value.toDouble())
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
                    ColorUtils.applyAlpha(accentColor.getColor1(), if (enabled) 164 else 92)
                )
            }
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

        if (!focused) {
            setting.setText(this.getText())
        }

        nvgInstance.restore()
        super.draw(mouseX, mouseY, partialTicks)
    }
}
