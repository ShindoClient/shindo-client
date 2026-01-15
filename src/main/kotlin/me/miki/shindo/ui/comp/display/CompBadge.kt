package me.miki.shindo.ui.comp.display

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.comp.templates.CompDisplay
import java.awt.Color

/**
 * Badge pequeno para mostrar status, contadores, etc.
 */
class CompBadge(
    text: String,
    x: Float = 0f,
    y: Float = 0f
) : CompDisplay(x, y) {

    private var text: String = text
    private var fontSize: Float = 8f
    private var backgroundColor: Color? = null
    private var textColor: Color? = null
    private var padding: Float = 6f
    private var radius: Float = 8f

    init {
        updateSize()
    }

    fun getText(): String = text
    fun setText(text: String): CompBadge {
        this.text = text
        updateSize()
        return this
    }

    fun setFontSize(size: Float): CompBadge {
        this.fontSize = size
        updateSize()
        return this
    }

    fun setBackgroundColor(color: Color?): CompBadge {
        this.backgroundColor = color
        return this
    }

    fun setTextColor(color: Color?): CompBadge {
        this.textColor = color
        return this
    }

    fun setPadding(padding: Float): CompBadge {
        this.padding = padding
        updateSize()
        return this
    }

    fun setRadius(radius: Float): CompBadge {
        this.radius = radius
        return this
    }

    private fun updateSize() {
        val nvgInstance = nvg
        val textWidth = nvgInstance.getTextWidth(text, fontSize, me.miki.shindo.management.nanovg.font.Fonts.REGULAR)
        val textHeight = nvgInstance.getTextHeight(text, fontSize, me.miki.shindo.management.nanovg.font.Fonts.REGULAR)
        setWidth(textWidth + padding * 2)
        setHeight(textHeight + padding * 2)
    }

    override fun drawDisplay(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent

        val bgColor = backgroundColor ?: accentColors.color1
        val txtColor = textColor ?: Color.WHITE

        nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), radius, bgColor)
        nvgInstance.drawCenteredText(
            text,
            getX() + getWidth() / 2f,
            getY() + getHeight() / 2f,
            txtColor,
            fontSize,
            me.miki.shindo.management.nanovg.font.Fonts.REGULAR
        )
    }
}
