package me.miki.shindo.gui.mainmenu.widget

import eu.shoroa.contrib.render.Blur
import me.miki.shindo.gui.widget.WidgetButtonBase
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Font
import me.miki.shindo.types.Color
import me.miki.shindo.utils.MathUtils
import org.lwjgl.nanovg.NanoVG


class WidgetMenuIconButton(
    iconFont: Font,
    icon: String,
    hoverColor: Color,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    onClick: Runnable
) : WidgetButtonBase(x, y, width, height, onClick) {
    private val iconFont: Font
    private val icon: String
    private val hoverColor: Color
    private val COLOR: Color = Color(0x78E0E0E0)
    private val TEXT_BASE: Color = Color(-1)
    private val TEXT: Color = Color(-1)

    override fun render(renderer: NanoVGManager, mouseX: Float, mouseY: Float) {
        super.render(renderer, mouseX, mouseY)
        if (isHovered) hoverAnimation.forceFinish()
        Color.Interpolate(TEXT_BASE, hoverColor, hoverAnimation.linearValue, TEXT)
        COLOR.setAlpha(0.3f + hoverAnimation.linearValue * 0.2f)
        Blur.drawBlur(getBounds(), 4.5f)
        renderer.drawRoundedRect(getBounds(), 4.5f, COLOR.toARGB())
        renderer.drawBlurredText(
            icon,
            getX() + getWidth() / 2f,
            getY() + getHeight() / 2f,
            MathUtils.interpolateARGB(-0x1000000, hoverColor.toARGB(), hoverAnimation.linearValue),
            12f,
            16f,
            NanoVG.NVG_ALIGN_CENTER or NanoVG.NVG_ALIGN_MIDDLE,
            iconFont
        )
        renderer.drawCenteredText(
            icon,
            getX() + getWidth() / 2f,
            getY() + getHeight() / 2f,
            TEXT.toARGB(),
            16f,
            iconFont
        )
    }

    init {
        this.iconFont = iconFont
        this.icon = icon
        this.hoverColor = hoverColor
    }
}