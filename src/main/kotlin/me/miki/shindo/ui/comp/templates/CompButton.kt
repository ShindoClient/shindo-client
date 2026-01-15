package me.miki.shindo.ui.comp.templates

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.comp.templates.CompInteractive
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import java.awt.Color

/**
 * Template para botões reutilizáveis.
 * Fornece animações, estados visuais e callbacks padronizados.
 */
open class CompButton(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : CompInteractive(x, y) {

    private val hoverAnimation = SimpleAnimation()
    private val clickAnimation = SimpleAnimation()
    private var text: String? = null
    private var backgroundColor: Color? = null
    private var hoverColor: Color? = null
    private var textColor: Color? = null
    private var radius: Float = 4f
    private var fontSize: Float = 10f

    init {
        setWidth(width)
        setHeight(height)
    }

    fun setText(text: String?): CompButton {
        this.text = text
        return this
    }

    fun setBackgroundColor(color: Color?): CompButton {
        this.backgroundColor = color
        return this
    }

    fun setHoverColor(color: Color?): CompButton {
        this.hoverColor = color
        return this
    }

    fun setTextColor(color: Color?): CompButton {
        this.textColor = color
        return this
    }

    fun setRadius(radius: Float): CompButton {
        this.radius = radius
        return this
    }

    fun setFontSize(size: Float): CompButton {
        this.fontSize = size
        return this
    }

    fun getText(): String? = text
    fun getTextColor(): Color? = textColor
    fun getFontSize(): Float = fontSize
    fun getRadius(): Float = radius

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent

        hoverAnimation.setAnimation(if (hovered && isEnabled()) 1.0f else 0.0f, 14.0)
        clickAnimation.setAnimation(if (clickAnimation.value > 0.1f) clickAnimation.value * 0.85f else 0.0f, 16.0)

        val baseBg = backgroundColor ?: paletteColors.getBackgroundColor(ColorType.NORMAL)
        val hoverBg = hoverColor ?: ColorUtils.applyAlpha(accentColors.color1, 180)

        val finalBg = if (hoverAnimation.value > 0.1f) {
            ColorUtils.interpolateColor(baseBg, hoverBg, hoverAnimation.value.toDouble())
        } else {
            baseBg
        }

        val finalBgWithClick = if (clickAnimation.value > 0.1f) {
            ColorUtils.darken(finalBg, clickAnimation.value * 0.2f)
        } else {
            finalBg
        }

        nvgInstance.drawRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            radius,
            if (isEnabled()) finalBgWithClick else ColorUtils.applyAlpha(finalBgWithClick, 120)
        )

        text?.let {
            val finalTextColor = textColor ?: paletteColors.getFontColor(
                ColorType.NORMAL,
                if (isEnabled()) 255 else 150
            )
            nvgInstance.drawCenteredText(
                it,
                getX() + getWidth() / 2f,
                getY() + getHeight() / 2f,
                finalTextColor,
                fontSize,
                me.miki.shindo.management.nanovg.font.Fonts.REGULAR
            )
        }
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        clickAnimation.value = 1.0f
    }
}
