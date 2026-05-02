package me.miki.shindo.ui.components.v1.templates

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.ui.components.v1.style.CompStyleResolver
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

open class CompPanel(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : Comp(x, y) {

    private var radius: Float = 8f
    private var backgroundColor: Color? = null
    private var shadowStrength: Int = 7
    private var borderWidth: Float = 0f
    private var borderColor: Color? = null
    private var padding: Float = 0f
    private var style: PanelStyle = PanelStyle.PANEL
    private var surfaceVariant: CompStyleResolver.CompSurfaceVariant = CompStyleResolver.CompSurfaceVariant.PANEL


    init {

        setWidth(width)
        setHeight(height)
    }

    open fun setRadius(radius: Float): CompPanel {
        this.radius = radius
        return this
    }

    fun getRadius(): Float = radius

    fun setBackgroundColor(color: Color?): CompPanel {
        this.backgroundColor = color
        return this
    }

    open fun setShadowStrength(strength: Int): CompPanel {
        this.shadowStrength = strength
        return this
    }

    fun setBorder(width: Float, color: Color?): CompPanel {
        this.borderWidth = width
        this.borderColor = color
        return this
    }

    fun setSurfaceVariant(surfaceVariant: CompStyleResolver.CompSurfaceVariant): CompPanel {
        this.surfaceVariant = surfaceVariant
        return this
    }

    fun getSurfaceVariant(): CompStyleResolver.CompSurfaceVariant = surfaceVariant

    fun setStyle(style: PanelStyle): CompPanel {
        this.style = style
        return this
    }

    fun getStyle(): PanelStyle = style

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return


        beforeDrawPanel(mouseX, mouseY, partialTicks)

        val bgColor = getBackgroundColor(palette, accent)
        val bdColor = getBorderColor(palette, accent)
        nvg.drawShadow(getX(), getY(), getWidth(), getHeight(), radius, shadowStrength)
        nvg.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), radius, bgColor!!)

        when (style) {
            PanelStyle.PANEL -> {
                nvg.drawRoundedRect(
                    getX() + 1, getY() + 1,
                    getWidth() - 2, getHeight() - 2,
                    radius - 1f,
                    bdColor!!
                )
            }

            PanelStyle.CARD -> {
                nvg.drawOutlineRoundedRect(
                    getX(), getY(),
                    getWidth(), getHeight(),
                    radius,
                    borderWidth,
                    bdColor!!
                )
            }
        }


        if (padding > 0f) {
            nvg.save()
            nvg.scissor(getX() + padding, getY() + padding, getWidth() - padding * 2, getHeight() - padding * 2)
        }

        drawPanelContent(mouseX, mouseY, partialTicks)

        if (padding > 0f) {
            nvg.restore()
        }

        super.draw(mouseX, mouseY, partialTicks)
    }

    fun setActualColors(backgroundColor: Color, borderColor: Color) {
        this.backgroundColor = backgroundColor
        this.borderColor = borderColor
    }

    protected open fun getBackgroundColor(
        paletteColors: ColorPalette,
        accentColors: AccentColor
    ): Color? {
        return backgroundColor ?: resolveDefaultBackground()
    }

    protected open fun getBorderColor(palette: ColorPalette, accent: AccentColor): Color? {
        return borderColor ?: resolveDefaultBorder()
    }

    protected open fun resolveDefaultBackground(): Color {
        return CompStyleResolver.resolveSurfaceBackground(surfaceVariant, palette, accent)
    }

    protected open fun resolveDefaultBorder(): Color {
        return PanelStyle.resolveStyleColor(style, palette, accent)
    }

    protected open fun beforeDrawPanel(mouseX: Int, mouseY: Int, partialTicks: Float) {}

    protected open fun drawPanelContent(mouseX: Int, mouseY: Int, partialTicks: Float) {}
}

enum class PanelStyle {
    PANEL,
    CARD;

    companion object {
        fun resolveStyleColor(style: PanelStyle, palette: ColorPalette, accent: AccentColor): Color {
            return when (style) {
                PANEL -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230)
                CARD -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210)
            }
        }
    }
}
