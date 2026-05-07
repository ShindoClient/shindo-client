package me.miki.shindo.ui.components.v2.display

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v1.value.SimpleAnimation
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.ColorUtils
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

@Suppress("UNUSED")
class CompTooltip(
    private var text: String,
    x: Float = 0f,
    y: Float = 0f
) : Comp(x, y) {

    private val fadeAnimation = SimpleAnimation()
    private var padding: Float = 8f
    private var fontSize: Float = 9f
    private var maxWidth: Float = 200f
    private var backgroundColor: Color? = null
    private var textColor: Color? = null
    private var radius: Float = 6f
    private var shadow: Boolean = true

    init {
        fadeAnimation.value = 0f
        updateSize()
    }

    fun getText(): String = text
    fun setText(text: String): CompTooltip {
        this.text = text
        updateSize()
        return this
    }

    fun setPadding(padding: Float): CompTooltip {
        this.padding = padding
        updateSize()
        return this
    }

    fun setFontSize(size: Float): CompTooltip {
        this.fontSize = size
        updateSize()
        return this
    }

    fun setMaxWidth(width: Float): CompTooltip {
        this.maxWidth = width
        updateSize()
        return this
    }

    fun setBackgroundColor(color: Color?): CompTooltip {
        this.backgroundColor = color
        return this
    }

    fun setTextColor(color: Color?): CompTooltip {
        this.textColor = color
        return this
    }

    fun setRadius(radius: Float): CompTooltip {
        this.radius = radius
        return this
    }

    fun setShadow(enabled: Boolean): CompTooltip {
        this.shadow = enabled
        return this
    }

    fun show() {
        fadeAnimation.setAnimation(1.0f, 12.0)
    }

    fun hide() {
        fadeAnimation.setAnimation(0.0f, 12.0)
    }

    private fun updateSize() {
        val nvgInstance = nvg
        val singleLineWidth = nvgInstance.getTextWidth(
            text,
            fontSize,
            Fonts.REGULAR
        )
        val contentWidth = max(60f, min(maxWidth, singleLineWidth + 2f))
        val textHeight = nvgInstance.getTextBoxHeight(
            text,
            fontSize,
            Fonts.REGULAR,
            contentWidth
        )
        setWidth(contentWidth + padding * 2)
        setHeight(textHeight + padding * 2)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible() || fadeAnimation.value <= 0f) return

        val nvgInstance = nvg
        val paletteColors = palette

        val alpha = (fadeAnimation.value * 255).toInt()
        val bgColor = backgroundColor ?: ColorUtils.applyAlpha(
            paletteColors.getBackgroundColor(ColorType.DARK),
            (alpha * 0.92f).toInt()
        )
        val txtColor = textColor ?: ColorUtils.applyAlpha(paletteColors.getFontColor(ColorType.NORMAL), alpha)

        if (shadow) {
            nvgInstance.drawShadow(getX(), getY(), getWidth(), getHeight(), radius, 5)
        }

        nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), radius, bgColor)
        nvgInstance.drawOutlineRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            radius,
            1f,
            ColorUtils.applyAlpha(paletteColors.getFontColor(ColorType.NORMAL), (alpha * 0.28f).toInt())
        )

        nvgInstance.drawTextBox(
            text,
            getX() + padding,
            getY() + padding,
            getWidth() - padding * 2f,
            txtColor,
            fontSize,
            Fonts.REGULAR
        )

        super.draw(mouseX, mouseY, partialTicks)
    }
}
