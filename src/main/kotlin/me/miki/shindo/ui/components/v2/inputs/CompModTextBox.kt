package me.miki.shindo.ui.components.v2.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.settings.impl.TextSetting
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.TimerUtils
import me.miki.shindo.utils.mouse.MouseUtils

class CompModTextBox : CompTextBoxBase {

    private val setting: TextSetting
    private val timer = TimerUtils()
    private val hoverAnim = SimpleAnimation()
    private val focusAnim = SimpleAnimation()


    constructor(x: Float, y: Float, width: Float, height: Float, setting: TextSetting) : super(x, y, width, height) {
        this.setting = setting
        this.setText(setting.getText())
    }

    constructor(setting: TextSetting) : super(0f, 0f, 0f, 0f) {
        this.setting = setting
        this.setText(setting.getText())
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val text = getText()
        val enabled = isEnabled()
        val focused = isFocused()
        val hovered = MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())

        val halfH = (getHeight() * 0.5f).coerceAtLeast(8f)
        val refText = text.ifEmpty { "A" }
        val textH = nvg.getTextHeight(refText, halfH, Fonts.REGULAR)
        val textY = getY() + getHeight() / 2f - textH / 2f + 0.5f
        val inset = 6f

        val addX = computeScrollOffset(text, halfH, inset)

        hoverAnim.setAnimation(if (hovered && enabled) 1f else 0f, 16.0)
        focusAnim.setAnimation(if (focused && enabled) 1f else 0f, 16.0)

        drawBackground(enabled)
        drawOutline(enabled)

        nvg.save()
        nvg.scissor(getX() + 2, getY(), getWidth() - 4, getHeight())

        drawSelection(text, halfH, textH, textY, inset, addX, enabled)
        drawText(text, halfH, textY, inset, addX, enabled)
        drawCursor(text, halfH, textH, textY, inset, addX, enabled, focused)

        nvg.restore()

        if (!focused) setting.setText(getText())

        super.draw(mouseX, mouseY, partialTicks)
    }

    private fun computeScrollOffset(text: String, halfH: Float, inset: Float): Float {
        val selEnd = getSelectionEnd()
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

    private fun drawBackground(enabled: Boolean) {
        val shell = ColorUtils.interpolateColor(
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210),
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 165),
            hoverAnim.getValue().toDouble()
        )
        var bg = ColorUtils.interpolateColor(
            shell,
            ColorUtils.applyAlpha(accent.getColor1(), 106),
            (focusAnim.getValue() * 0.18f).toDouble()
        )
        if (!enabled) bg = ColorUtils.applyAlpha(bg, 116)

        nvg.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 6f, bg)
    }

    private fun drawOutline(enabled: Boolean) {
        val mixed = ColorUtils.interpolateColor(
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 28),
            ColorUtils.applyAlpha(accent.getColor1(), 76),
            hoverAnim.getValue().toDouble()
        )
        var outline = ColorUtils.interpolateColor(
            mixed,
            ColorUtils.applyAlpha(accent.getColor1(), 154),
            focusAnim.getValue().toDouble()
        )
        if (!enabled) outline = ColorUtils.applyAlpha(outline, 96)

        nvg.drawOutlineRoundedRect(getX(), getY(), getWidth(), getHeight(), 6f, 1f, outline)
    }

    private fun drawSelection(
        text: String, halfH: Float, textH: Float, textY: Float, inset: Float, addX: Float, enabled: Boolean
    ) {
        val cursor = getCursorPosition()
        val selEnd = getSelectionEnd()
        if (cursor == selEnd) return

        val start = minOf(selEnd, cursor)
        val end = maxOf(selEnd, cursor)
        val selW = nvg.getTextWidth(text.substring(start, end), halfH, Fonts.REGULAR)
        val offset = nvg.getTextWidth(text.substring(0, start), halfH, Fonts.REGULAR)

        if (selW != 0f) {
            nvg.drawRect(
                getX() + inset + offset + addX, textY - 0.5f,
                selW, textH + 1f,
                ColorUtils.applyAlpha(accent.getColor1(), if (enabled) 164 else 92)
            )
        }
    }

    private fun drawText(text: String, halfH: Float, textY: Float, inset: Float, addX: Float, enabled: Boolean) {
        val color = if (enabled) palette.getFontColor(ColorType.DARK)
        else palette.getFontColor(ColorType.NORMAL, 145)
        nvg.drawText(text, getX() + inset + addX, textY, color, halfH, Fonts.REGULAR)
    }

    private fun drawCursor(
        text: String,
        halfH: Float,
        textH: Float,
        textY: Float,
        inset: Float,
        addX: Float,
        enabled: Boolean,
        focused: Boolean
    ) {
        if (!enabled || !focused || getCursorPosition() != getSelectionEnd()) return
        if (!timer.delay(600)) return

        val pos = nvg.getTextWidth(text, halfH, Fonts.REGULAR) -
                nvg.getTextWidth(text.substring(getCursorPosition()), halfH, Fonts.REGULAR)

        nvg.drawRect(
            getX() + inset + addX + pos, textY - 0.5f,
            0.85f, textH + 1.25f,
            palette.getFontColor(ColorType.DARK)
        )

        if (timer.delay(1200)) timer.reset()
    }
}