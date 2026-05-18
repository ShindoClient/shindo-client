package me.miki.shindo.ui.components.v2.templates

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.components.v2.Component
import me.miki.shindo.utils.ColorUtils

open class CompPanel(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f,
) : Component(x, y) {
    private var radius: Float = 8f
    private var shadowStrength: Int = 7
    private var borderWidth: Float = 0f
    private var style: PanelStyle = PanelStyle.PANEL

    init {
        setWidth(width)
        setHeight(height)
    }

    open fun getRadius(): Float = radius

    open fun setRadius(radius: Float) {
        this.radius = radius
    }

    open fun setShadowStrength(strength: Int) {
        this.shadowStrength = strength
    }

    open fun setStyle(style: PanelStyle) {
        this.style = style
    }

    override fun draw(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        if (!isVisible()) return

        beforeDrawPanel(mouseX, mouseY, partialTicks)

        nvg.drawShadow(getX(), getY(), getWidth(), getHeight(), radius, shadowStrength)
        nvg.drawRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            radius,
            when (style) {
                PanelStyle.PANEL -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210)
                PanelStyle.CARD -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220)
            },
        )

        when (style) {
            PanelStyle.PANEL -> {
                nvg.drawRoundedRect(
                    getX() + 1,
                    getY() + 1,
                    getWidth() - 2,
                    getHeight() - 2,
                    radius - 1f,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230),
                )
            }

            PanelStyle.CARD -> {
                nvg.drawOutlineRoundedRect(
                    getX(),
                    getY(),
                    getWidth(),
                    getHeight(),
                    radius,
                    borderWidth,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210),
                )
            }
        }

        nvg.save()
        nvg.intersectScissor(getX(), getY(), getWidth(), getHeight())
        drawPanelContent(mouseX, mouseY, partialTicks)
        nvg.restore()

        super.draw(mouseX, mouseY, partialTicks)
    }

    protected open fun beforeDrawPanel(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {}

    protected open fun drawPanelContent(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {}
}

enum class PanelStyle {
    PANEL,
    CARD,
}
