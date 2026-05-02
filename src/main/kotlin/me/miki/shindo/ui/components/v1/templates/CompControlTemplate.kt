package me.miki.shindo.ui.components.v1.templates

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v1.value.SimpleAnimation
import me.miki.shindo.ui.components.v1.style.CompControlVariant
import me.miki.shindo.ui.components.v1.style.CompStyleResolver
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.ColorUtils

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
    private var variant: CompControlVariant = CompControlVariant.SECONDARY

    init {
        Comp.setWidth(width)
        Comp.setHeight(height)
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

    fun setVariant(variant: CompControlVariant): CompControlTemplate {
        this.variant = variant
        return this
    }

    fun getVariant(): CompControlVariant = variant

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        hoverAnimation.setAnimation(if (hovered && isEnabled()) 1.0f else 0.0f, 16.0)
        pressAnimation.setAnimation(if (pressAnimation.value > 0.1f) pressAnimation.value * 0.84f else 0.0f, 16.0)

        val base = CompStyleResolver.resolveControlBase(variant, Comp.palette, Comp.accent)
        val hover = CompStyleResolver.resolveControlHover(variant, Comp.palette, Comp.accent)
        val background = ColorUtils.interpolateColor(base, hover, hoverAnimation.value.toDouble())
        val pressed =
            if (pressAnimation.value > 0.1f) ColorUtils.darken(background, pressAnimation.value * 0.2f) else background

        Comp.nvg.drawRoundedRect(
            Comp.getX(),
            Comp.getY(),
            Comp.getWidth(),
            Comp.getHeight(),
            radius,
            if (isEnabled()) pressed else ColorUtils.applyAlpha(pressed, 130)
        )

        val drawText = text
        if (!drawText.isNullOrEmpty()) {
            val color = if (isEnabled()) {
                CompStyleResolver.resolveControlText(variant, Comp.palette)
            } else {
                Comp.palette.getFontColor(ColorType.NORMAL, 150)
            }

            Comp.nvg.drawCenteredText(
                drawText,
                Comp.getX() + Comp.getWidth() / 2f,
                Comp.getY() + Comp.getHeight() / 2f - fontSize / 2f,
                color,
                fontSize,
                Fonts.REGULAR
            )
        }
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        pressAnimation.value = 1.0f
    }
}
