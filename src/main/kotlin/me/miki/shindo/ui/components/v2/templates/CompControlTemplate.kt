package me.miki.shindo.ui.components.v2.templates

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

open class CompControlTemplate(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : CompInteractive(x, y) {

    private val hoverAnimation = SimpleAnimation()
    private val pressAnimation = SimpleAnimation()

    private var text: String? = null
    private var radius = 6f
    private var fontSize = 10f

    init {
        setWidth(width)
        setHeight(height)
    }

    fun setText(text: String?): CompControlTemplate {
        this.text = text
        return this
    }

    fun getText(): String? = text

    open fun setRadius(radius: Float): CompControlTemplate {
        this.radius = radius
        return this
    }

    open fun setFontSize(fontSize: Float): CompControlTemplate {
        this.fontSize = fontSize
        return this
    }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        hoverAnimation.setAnimation(if (hovered && isEnabled()) 1.0f else 0.0f, 16.0)
        pressAnimation.setAnimation(if (pressAnimation.getValue() > 0.1f) pressAnimation.getValue() * 0.84f else 0.0f, 16.0)


        val background = ColorUtils.interpolateColor(ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210), ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 165), hoverAnimation.getValue().toDouble())
        val pressed = if (pressAnimation.getValue() > 0.1f) ColorUtils.darken(background, pressAnimation.getValue() * 0.2f) else background

        nvg.drawRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            radius,
            if (isEnabled()) pressed else ColorUtils.applyAlpha(pressed, 130)
        )

        val drawText = text
        if (!drawText.isNullOrEmpty()) {
            val color = if (isEnabled()) {
                Color.WHITE
            } else {
                palette.getFontColor(ColorType.NORMAL, 150)
            }

            nvg.drawCenteredText(
                drawText,
                getX() + getWidth() / 2f,
                getY() + getHeight() / 2f - fontSize / 2f,
                color,
                fontSize,
                Fonts.REGULAR
            )
        }
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        pressAnimation.setValue(1.0f)
    }
}
