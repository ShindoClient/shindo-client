package me.miki.shindo.ui.components.v2.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.TimerUtils

open class CompTextBox(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : CompTextBoxBase(x, y, width, height) {

    private val timer = TimerUtils()
    private val hintAnim = SimpleAnimation()

    private var defaultText: String? = null
    private var validator: ((String) -> Boolean)? = null


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

    fun hasValidationError(): Boolean = validator?.let { !it(getText()) } ?: false

    override fun setPosition(x: Float, y: Float, width: Float, height: Float) {
        setX(x); setY(y); setWidth(width); setHeight(height)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val text = getText()
        val enabled = isEnabled()
        val focused = isFocused()
        val halfH = (getHeight() * 0.5f).coerceAtLeast(8f)
        val textH = nvg.getTextHeight(text.ifEmpty { "A" }, halfH, Fonts.REGULAR)
        val textY = getY() + getHeight() / 2f - textH / 2f + 0.5f
        val inset = 5f

        val addX = computeScrollOffset(text, halfH)

        nvg.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 6f, palette.getBackgroundColor(ColorType.NORMAL))

        nvg.save()
        nvg.scissor(getX() + 2, getY(), getWidth() - 4, getHeight())

        drawSelection(text, halfH, textH, textY, inset, addX, enabled)
        drawHint(text, halfH, textY, enabled, focused)
        drawText(text, halfH, textY, inset, addX)
        drawCursor(text, halfH, textH, textY, inset, addX, focused)

        nvg.restore()
        super.draw(mouseX, mouseY, partialTicks)
    }

    private fun computeScrollOffset(text: String, halfH: Float): Float {
        val selEnd = getSelectionEnd()
        val inset = 6f
        var addX = 0f
        var result = ""

        for (c in text) {
            result += c
            if (nvg.getTextWidth(result, halfH, Fonts.REGULAR) + inset * 2 > getWidth()) {
                addX = getWidth() - nvg.getTextWidth(result, halfH, Fonts.REGULAR) - inset * 2
            }
        }

        val outTextSize = text.length - result.length
        if (selEnd < outTextSize) {
            addX = getWidth() - nvg.getTextWidth(
                text.reversed().substring(outTextSize - selEnd), halfH, Fonts.REGULAR
            ) - inset * 2
        }

        return addX
    }

    private fun drawSelection(
        text: String,
        halfH: Float,
        textH: Float,
        textY: Float,
        inset: Float,
        addX: Float,
        enabled: Boolean
    ) {
        val cursor = getCursorPosition()
        val selEnd = getSelectionEnd()
        if (cursor == selEnd) return

        val start = minOf(cursor, selEnd)
        val end = maxOf(cursor, selEnd)
        val selW = nvg.getTextWidth(text.substring(start, end), halfH, Fonts.REGULAR)
        val offset = nvg.getTextWidth(text.substring(0, start), halfH, Fonts.REGULAR)

        if (selW != 0f) {
            nvg.drawRect(
                getX() + inset + offset + addX, textY - 0.5f,
                selW, textH + 1f,
                ColorUtils.applyAlpha(accent.getColor1(), if (enabled) 160 else 94)
            )
        }
    }

    private fun drawHint(text: String, halfH: Float, textY: Float, enabled: Boolean, focused: Boolean) {
        val hint = defaultText ?: return
        hintAnim.setAnimation(if (!focused && text.isEmpty()) 1f else 0f, 16.0)
        if (text.isNotEmpty()) return

        val alpha = (hintAnim.getValue() * if (enabled) 200f else 140f).toInt().coerceIn(0, 255)
        nvg.save()
        nvg.translate(hintAnim.getValue() * 8f - 8f, 0f)
        nvg.drawText(hint, getX() + 5, textY, palette.getFontColor(ColorType.DARK, alpha), halfH, Fonts.REGULAR)
        nvg.restore()
    }

    private fun drawText(text: String, halfH: Float, textY: Float, inset: Float, addX: Float) {
        nvg.drawText(text, getX() + inset + addX, textY, palette.getFontColor(ColorType.DARK), halfH, Fonts.REGULAR)
    }

    private fun drawCursor(
        text: String,
        halfH: Float,
        textH: Float,
        textY: Float,
        inset: Float,
        addX: Float,
        focused: Boolean
    ) {
        if (!focused || getCursorPosition() != getSelectionEnd()) return
        if (!timer.delay(600)) return

        val pos = nvg.getTextWidth(text, halfH, Fonts.REGULAR) -
                nvg.getTextWidth(text.substring(getCursorPosition()), halfH, Fonts.REGULAR)

        nvg.drawRect(getX() + inset + addX + pos, textY - 0.5f, 0.7f, 10f, palette.getFontColor(ColorType.DARK))

        if (timer.delay(1200)) timer.reset()
    }
}