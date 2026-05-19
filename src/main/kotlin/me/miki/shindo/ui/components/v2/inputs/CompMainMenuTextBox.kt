package me.miki.shindo.ui.components.v2.inputs

import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.TimerUtils
import java.awt.Color

class CompMainMenuTextBox(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f,
) : CompTextBoxBase(x, y, width, height) {
    private val timer = TimerUtils()
    private val animation = SimpleAnimation()

    var backgroundColor: Color = Color.WHITE
    var fontColor: Color = Color.WHITE
    var passwordMode: Boolean = false

    private var icon: String? = null
    private var title: String? = null

    fun setEmptyText(
        icon: String,
        title: String,
    ) {
        this.icon = icon
        this.title = title
    }

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
        val rawText = getText()
        val drawText = if (passwordMode) "*".repeat(rawText.length) else rawText
        val focused = isFocused()
        val halfH = getHeight() / 2f

        val addX = computeScrollOffset(drawText, halfH) + if (icon != null && title != null) 16 else 5

        nvg.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 4f, backgroundColor)

        nvg.save()
        nvg.scissor(getX() + 1, getY(), getWidth() - 2, getHeight())

        drawSelection(drawText, halfH, addX)
        drawPlaceholder(rawText, drawText, halfH, focused)
        drawText(drawText, halfH, addX)
        drawCursor(drawText, halfH, addX, focused)

        nvg.restore()
        super.draw(mouseX, mouseY, partialTicks)
    }

    private fun computeScrollOffset(
        drawText: String,
        halfH: Float,
    ): Float {
        val selectionEnd = getSelectionEnd()
        var addX = 0f
        var result = ""

        for (c in drawText) {
            result += c
            if (nvg.getTextWidth(result, halfH, Fonts.REGULAR) + halfH + 5 > getWidth()) {
                addX = getWidth() - nvg.getTextWidth(result, halfH, Fonts.REGULAR) - halfH - 5
            }
        }

        val outTextSize = drawText.length - result.length
        if (selectionEnd < outTextSize) {
            val reversed = drawText.reversed()
            addX = getWidth() -
                nvg.getTextWidth(
                    reversed.substring(outTextSize - selectionEnd),
                    halfH,
                    Fonts.REGULAR,
                ) - halfH - 5
        }

        return addX
    }

    private fun drawSelection(
        drawText: String,
        halfH: Float,
        addX: Float,
    ) {
        val cursor = getCursorPosition()
        val selEnd = getSelectionEnd()
        if (cursor == selEnd) return

        val start = minOf(selEnd, cursor)
        val end = maxOf(selEnd, cursor)
        val selWidth = nvg.getTextWidth(drawText.substring(start, end), halfH, Fonts.REGULAR)
        val offset = nvg.getTextWidth(drawText.substring(0, start), halfH, Fonts.REGULAR)

        if (selWidth != 0f) {
            nvg.drawRect(
                getX() + offset + addX - 1,
                getY() + getHeight() / 2 - nvg.getTextHeight(drawText, halfH, Fonts.REGULAR) / 2,
                selWidth,
                nvg.getTextHeight(drawText, halfH, Fonts.REGULAR),
                Color(0, 135, 247),
            )
        }
    }

    private fun drawPlaceholder(
        rawText: String,
        drawText: String,
        halfH: Float,
        focused: Boolean,
    ) {
        val icon = icon ?: return
        val title = title ?: return

        val textY = getY() + getHeight() / 2 - nvg.getTextHeight(drawText, halfH, Fonts.REGULAR) / 2

        nvg.drawText(icon, getX() + 5, textY, fontColor, halfH, Fonts.LUCIDE)

        animation.setAnimation(if (!focused && rawText.isEmpty()) 1f else 0f, 16.0)

        if (rawText.isEmpty()) {
            nvg.save()
            nvg.translate(animation.getValue() * 8 - 8, 0f)
            nvg.drawText(
                title,
                getX() + 16,
                textY + 1,
                ColorUtils.applyAlpha(fontColor, (animation.getValue() * 255).toInt()),
                halfH,
                Fonts.REGULAR,
            )
            nvg.restore()
        }
    }

    private fun drawText(
        drawText: String,
        halfH: Float,
        addX: Float,
    ) {
        nvg.drawText(
            drawText,
            getX() + addX,
            getY() + getHeight() / 2 - nvg.getTextHeight(drawText, halfH, Fonts.REGULAR) / 2 + 1,
            fontColor,
            halfH,
            Fonts.REGULAR,
        )
    }

    private fun drawCursor(
        drawText: String,
        halfH: Float,
        addX: Float,
        focused: Boolean,
    ) {
        if (!focused || getCursorPosition() != getSelectionEnd()) return
        if (!timer.delay(600)) return

        val position = nvg.getTextWidth(drawText.substring(0, getCursorPosition()), halfH, Fonts.REGULAR)
        nvg.drawRect(
            getX() + addX + position,
            getY() + getHeight() / 2 - nvg.getTextHeight(drawText, halfH, Fonts.REGULAR) / 2,
            0.7f,
            10f,
            fontColor,
        )

        if (timer.delay(1200)) timer.reset()
    }
}
