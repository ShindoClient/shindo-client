package me.miki.shindo.ui.components.v2.inputs

import me.miki.extensions.ui.animation.setAnimation
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.TimerUtils

class CompSearchBox(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f,
) : CompTextBoxBase(x, y, width, height) {
    private val timer = TimerUtils()
    private val searchAnim = SimpleAnimation()

    override fun setPosition(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        setX(x)
        setY(y)
        setWidth(width)
        setHeight(height)
    }

    override fun draw(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val text = getText()
        val focused = isFocused()
        val halfH = getHeight() / 2f
        val textH = nvg.getTextHeight(text, halfH, Fonts.REGULAR)
        val textY = getY() + getHeight() / 2f - textH / 2f

        val addX = computeScrollOffset(text, halfH)

        nvg.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 6f, palette.getBackgroundColor(ColorType.DARK))

        nvg.save()
        nvg.scissor(getX() + 1, getY(), getWidth() - 2, getHeight())

        drawSelection(text, halfH, textH, textY, addX)
        drawPlaceholder(text, halfH, textY, focused)
        drawText(text, halfH, textY, addX)
        drawCursor(text, halfH, textY, addX, focused)

        nvg.restore()
        super.draw(mouseX, mouseY, partialTicks)
    }

    private fun computeScrollOffset(
        text: String,
        halfH: Float,
    ): Float {
        val selEnd = getSelectionEnd()
        var addX = 0f
        var result = ""

        for (c in text) {
            result += c
            if (nvg.getTextWidth(result, halfH, Fonts.REGULAR) + halfH + 5 > getWidth()) {
                addX = getWidth() - nvg.getTextWidth(result, halfH, Fonts.REGULAR) - halfH - 5
            }
        }

        val outTextSize = text.length - result.length
        if (selEnd < outTextSize) {
            addX = getWidth() -
                nvg.getTextWidth(
                    text.reversed().substring(outTextSize - selEnd),
                    halfH,
                    Fonts.REGULAR,
                ) - halfH - 5
        }

        return addX
    }

    private fun drawSelection(
        text: String,
        halfH: Float,
        textH: Float,
        textY: Float,
        addX: Float,
    ) {
        val cursor = getCursorPosition()
        val selEnd = getSelectionEnd()
        if (cursor == selEnd) return

        val start = minOf(cursor, selEnd)
        val end = maxOf(cursor, selEnd)
        val selW = nvg.getTextWidth(text.substring(start, end), halfH, Fonts.REGULAR)
        val offset = nvg.getTextWidth(text.substring(0, start), halfH, Fonts.REGULAR)

        if (selW != 0f) {
            nvg.drawRect(getX() + 15 + offset + addX, textY, selW, textH, java.awt.Color(0, 135, 247))
        }
    }

    private fun drawPlaceholder(
        text: String,
        halfH: Float,
        textY: Float,
        focused: Boolean,
    ) {
        nvg.drawText(
            LegacyIcon.SEARCH,
            getX() + 5,
            textY,
            palette.getFontColor(ColorType.NORMAL),
            halfH,
            Fonts.LEGACYICON,
        )

        searchAnim.setAnimation(if (!focused && text.isEmpty()) 1f else 0f, 16)

        if (text.isEmpty()) {
            nvg.save()
            nvg.translate(searchAnim.getValue() * 8 - 8, 0f)
            nvg.drawText(
                TranslateText.SEARCH.getText(),
                getX() + 16,
                textY + 1,
                palette.getFontColor(ColorType.NORMAL, (searchAnim.getValue() * 200).toInt()),
                halfH,
                Fonts.REGULAR,
            )
            nvg.restore()
        }
    }

    private fun drawText(
        text: String,
        halfH: Float,
        textY: Float,
        addX: Float,
    ) {
        nvg.drawText(text, getX() + 16 + addX, textY + 1, palette.getFontColor(ColorType.NORMAL), halfH, Fonts.REGULAR)
    }

    private fun drawCursor(
        text: String,
        halfH: Float,
        textY: Float,
        addX: Float,
        focused: Boolean,
    ) {
        if (!focused || getCursorPosition() != getSelectionEnd()) return
        if (!timer.delay(600)) return

        val pos =
            nvg.getTextWidth(text, halfH, Fonts.REGULAR) -
                nvg.getTextWidth(text.substring(getCursorPosition()), halfH, Fonts.REGULAR)

        nvg.drawRect(getX() + 16 + addX + pos, textY - 0.5f, 0.7f, 10f, palette.getFontColor(ColorType.DARK))

        if (timer.delay(1200)) timer.reset()
    }
}