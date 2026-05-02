package me.miki.shindo.ui.components.v1.templates

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v1.value.SimpleAnimation
import me.miki.shindo.ui.components.v1.style.CompControlVariant
import me.miki.shindo.ui.components.v1.style.CompStyleResolver
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

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
    private var variant: CompControlVariant = CompControlVariant.SECONDARY

    init {
        Comp.setWidth(width)
        Comp.setHeight(height)
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

    fun setVariant(variant: CompControlVariant): CompButton {
        this.variant = variant
        return this
    }

    fun getVariant(): CompControlVariant = variant

    fun getText(): String? = text
    fun getTextColor(): Color? = textColor
    fun getFontSize(): Float = fontSize
    fun getRadius(): Float = radius

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val nvgInstance = Comp.nvg
        val paletteColors = Comp.palette
        val accentColors = Comp.accent

        hoverAnimation.setAnimation(if (hovered && isEnabled()) 1.0f else 0.0f, 14.0)
        clickAnimation.setAnimation(if (clickAnimation.value > 0.1f) clickAnimation.value * 0.85f else 0.0f, 16.0)

        val baseBg = backgroundColor ?: CompStyleResolver.resolveControlBase(variant, paletteColors, accentColors)
        val hoverBg = hoverColor ?: CompStyleResolver.resolveControlHover(variant, paletteColors, accentColors)

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
            Comp.getX(),
            Comp.getY(),
            Comp.getWidth(),
            Comp.getHeight(),
            radius,
            if (isEnabled()) finalBgWithClick else ColorUtils.applyAlpha(finalBgWithClick, 120)
        )

        text?.let {
            val finalTextColor = textColor ?: if (isEnabled()) {
                CompStyleResolver.resolveControlText(variant, paletteColors)
            } else {
                paletteColors.getFontColor(ColorType.NORMAL, 150)
            }

            val textY = Comp.getY() + Comp.getHeight() / 2f - fontSize / 2f
            nvgInstance.drawCenteredText(
                it,
                Comp.getX() + Comp.getWidth() / 2f,
                textY,
                finalTextColor,
                fontSize,
                Fonts.REGULAR
            )
        }
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        clickAnimation.value = 1.0f
    }
}
