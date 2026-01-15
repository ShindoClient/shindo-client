package me.miki.shindo.ui.comp.templates

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.templates.CompDisplay
import java.awt.Color

/**
 * Template para labels de texto simples.
 * Suporta diferentes tamanhos, cores e alinhamentos.
 */
open class CompLabel(
    x: Float = 0f,
    y: Float = 0f,
    text: String = ""
) : CompDisplay(x, y) {

    private var text: String = text
    private var fontSize: Float = 10f
    private var fontColor: Color? = null
    private var font: me.miki.shindo.management.nanovg.font.Font = Fonts.REGULAR
    private var alignment: TextAlignment = TextAlignment.LEFT
    private var shadow: Boolean = false
    private var shadowColor: Color? = null
    private var shadowOffset: Float = 1f

    enum class TextAlignment {
        LEFT, CENTER, RIGHT
    }

    fun getText(): String = text
    fun setText(text: String): CompLabel {
        this.text = text
        updateSize()
        return this
    }

    fun setFontSize(size: Float): CompLabel {
        this.fontSize = size
        updateSize()
        return this
    }

    fun setFontColor(color: Color?): CompLabel {
        this.fontColor = color
        return this
    }

    fun setFont(font: me.miki.shindo.management.nanovg.font.Font): CompLabel {
        this.font = font
        updateSize()
        return this
    }

    fun setAlignment(alignment: TextAlignment): CompLabel {
        this.alignment = alignment
        return this
    }

    fun setShadow(enabled: Boolean, color: Color? = null, offset: Float = 1f): CompLabel {
        this.shadow = enabled
        this.shadowColor = color
        this.shadowOffset = offset
        return this
    }

    private fun updateSize() {
        val nvgInstance = nvg
        val textWidth = nvgInstance.getTextWidth(text, fontSize, font)
        val textHeight = nvgInstance.getTextHeight(text, fontSize, font)
        setWidth(textWidth)
        setHeight(textHeight)
    }

    override fun drawDisplay(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val paletteColors = palette

        val finalColor = fontColor ?: paletteColors.getFontColor(ColorType.NORMAL)
        val x = when (alignment) {
            TextAlignment.LEFT -> getX()
            TextAlignment.CENTER -> getX() + getWidth() / 2f
            TextAlignment.RIGHT -> getX() + getWidth()
        }

        if (shadow) {
            val shadowCol = shadowColor ?: Color(0, 0, 0, 100)
            nvgInstance.drawText(
                text,
                x + shadowOffset,
                getY() + shadowOffset,
                shadowCol,
                fontSize,
                font
            )
        }

        when (alignment) {
            TextAlignment.LEFT -> nvgInstance.drawText(text, x, getY(), finalColor, fontSize, font)
            TextAlignment.CENTER -> nvgInstance.drawCenteredText(text, x, getY(), finalColor, fontSize, font)
            TextAlignment.RIGHT -> {
                val textWidth = nvgInstance.getTextWidth(text, fontSize, font)
                nvgInstance.drawText(text, x - textWidth, getY(), finalColor, fontSize, font)
            }
        }
    }
}
