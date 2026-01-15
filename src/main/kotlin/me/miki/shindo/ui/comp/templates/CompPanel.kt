package me.miki.shindo.ui.comp.templates

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

/**
 * Template para painéis com fundo, sombra e bordas arredondadas.
 * Útil para criar áreas de conteúdo destacadas.
 */
open class CompPanel(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : Comp(x, y) {

    private var radius: Float = 8f
    private var backgroundColor: Color? = null
    private var shadowEnabled: Boolean = true
    private var shadowStrength: Int = 7
    private var borderWidth: Float = 0f
    private var borderColor: Color? = null
    private var padding: Float = 0f

    init {
        setWidth(width)
        setHeight(height)
    }

    fun setRadius(radius: Float): CompPanel {
        this.radius = radius
        return this
    }

    fun getRadius(): Float = radius

    fun setBackgroundColor(color: Color?): CompPanel {
        this.backgroundColor = color
        return this
    }

    fun setShadowEnabled(enabled: Boolean): CompPanel {
        this.shadowEnabled = enabled
        return this
    }

    fun setShadowStrength(strength: Int): CompPanel {
        this.shadowStrength = strength
        return this
    }

    fun setBorder(width: Float, color: Color?): CompPanel {
        this.borderWidth = width
        this.borderColor = color
        return this
    }

    fun setPadding(padding: Float): CompPanel {
        this.padding = padding
        return this
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return

        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent

        val bgColor = getBackgroundColor(paletteColors, accentColors)

        if (shadowEnabled) {
            nvgInstance.drawShadow(getX(), getY(), getWidth(), getHeight(), radius, shadowStrength)
        }

        if (bgColor != null) {
            nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), radius, bgColor)
        }

        if (borderWidth > 0f && borderColor != null) {
            nvgInstance.drawOutlineRoundedRect(
                getX(),
                getY(),
                getWidth(),
                getHeight(),
                radius,
                borderWidth,
                borderColor!!
            )
        }

        if (padding > 0f) {
            nvgInstance.save()
            nvgInstance.scissor(getX() + padding, getY() + padding, getWidth() - padding * 2, getHeight() - padding * 2)
        }

        drawPanelContent(mouseX, mouseY, partialTicks)

        if (padding > 0f) {
            nvgInstance.restore()
        }

        super.draw(mouseX, mouseY, partialTicks)
    }

    /**
     * Método para obter a cor de fundo. Pode ser sobrescrito para customização.
     */
    protected open fun getBackgroundColor(paletteColors: me.miki.shindo.management.color.palette.ColorPalette, accentColors: me.miki.shindo.management.color.AccentColor): Color? {
        return backgroundColor ?: paletteColors.getBackgroundColor(ColorType.DARK)
    }

    /**
     * Método para renderizar conteúdo específico do painel.
     * Pode ser sobrescrito por subclasses.
     */
    protected open fun drawPanelContent(mouseX: Int, mouseY: Int, partialTicks: Float) {}
}
