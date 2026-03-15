package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.comp.style.CompControlVariant
import me.miki.shindo.ui.comp.style.CompStyleResolver
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.TimerUtils
import me.miki.shindo.utils.mouse.MouseUtils

class CompSearchBox : CompTextBoxBase {
    private val timer = TimerUtils()
    private val searchAnimation = SimpleAnimation()
    private val hoverAnimation = SimpleAnimation()
    private val focusAnimation = SimpleAnimation()

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
        val accentColors = accent

        val height = getHeight()
        val selectionEnd = this.getSelectionEnd()
        val cursorPosition = this.getCursorPosition()
        val text = this.getText()
        val focused = this.isFocused()
        val hovered = MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())
        val textInset = 20f
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

        hoverAnimation.setAnimation(if (hovered) 1.0f else 0.0f, 16.0)
        focusAnimation.setAnimation(if (focused) 1.0f else 0.0f, 16.0)

        val baseBackground = CompStyleResolver.resolveControlBase(CompControlVariant.GHOST, paletteColors, accentColors)
        val hoverBackground =
            CompStyleResolver.resolveControlHover(CompControlVariant.GHOST, paletteColors, accentColors)
        val shellColor = ColorUtils.interpolateColor(baseBackground, hoverBackground, hoverAnimation.value.toDouble())
        val focusTint = ColorUtils.applyAlpha(accentColors.getColor1(), 128)
        val background = ColorUtils.interpolateColor(shellColor, focusTint, (focusAnimation.value * 0.22f).toDouble())
        val idleOutline = ColorUtils.applyAlpha(paletteColors.getFontColor(ColorType.NORMAL), 26)
        val hoverOutline = ColorUtils.applyAlpha(accentColors.getColor1(), 72)
        val focusOutline = ColorUtils.applyAlpha(accentColors.getColor1(), 150)
        val mixedOutline = ColorUtils.interpolateColor(idleOutline, hoverOutline, hoverAnimation.value.toDouble())
        val outlineColor = ColorUtils.interpolateColor(mixedOutline, focusOutline, focusAnimation.value.toDouble())

        nvgInstance.drawRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            6f,
            background
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
                    ColorUtils.applyAlpha(accentColors.getColor1(), 166)
                )
            }
        }

        searchAnimation.setAnimation(if (!focused && text.isEmpty()) 1.0f else 0.0f, 16.0)
        val iconColor = ColorUtils.interpolateColor(
            paletteColors.getFontColor(ColorType.NORMAL, 168),
            paletteColors.getFontColor(ColorType.NORMAL),
            maxOf(hoverAnimation.value, focusAnimation.value).toDouble()
        )
        val iconSize = (halfHeight + 0.5f).coerceAtLeast(8f)
        val iconY =
            getY() + getHeight() / 2f - nvgInstance.getTextHeight(LegacyIcon.SEARCH, iconSize, Fonts.LEGACYICON) / 2f

        nvgInstance.drawText(
            LegacyIcon.SEARCH,
            getX() + 6f,
            iconY,
            iconColor,
            iconSize,
            Fonts.LEGACYICON
        )

        if (text.isEmpty()) {
            nvgInstance.save()
            nvgInstance.translate(searchAnimation.value * 6f - 6f, 0f)
            val placeholderAlpha = (searchAnimation.value * 206f).toInt().coerceIn(0, 206)
            nvgInstance.drawText(
                TranslateText.SEARCH.getText(),
                getX() + textInset,
                textY,
                paletteColors.getFontColor(ColorType.NORMAL, placeholderAlpha),
                halfHeight,
                Fonts.REGULAR
            )
            nvgInstance.restore()
        }

        nvgInstance.drawText(
            text,
            getX() + textInset + addX,
            textY,
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
                    getX() + textInset + addX + position,
                    textY - 0.5f,
                    0.85f,
                    textHeight + 1.25f,
                    paletteColors.getFontColor(ColorType.DARK, 236)
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
