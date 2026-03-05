package me.miki.shindo.ui.comp.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.comp.style.CompControlVariant
import me.miki.shindo.ui.comp.style.CompStyleResolver
import me.miki.shindo.ui.comp.templates.CompButton
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

class CompActionButton(
    text: String,
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 80f,
    height: Float = 20f
) : CompButton(x, y, width, height) {

    private val hoverAnimation = SimpleAnimation()
    private val pressAnimation = SimpleAnimation()

    init {
        setRadius(6f)
        setFontSize(10f)
        setVariant(CompControlVariant.PRIMARY)
        setText(text)
        setTextColor(Color.WHITE)
    }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent
        val enabled = isEnabled()
        val variant = getVariant()

        hoverAnimation.setAnimation(if (hovered && enabled) 1.0f else 0.0f, 14.0)
        pressAnimation.setAnimation(if (pressAnimation.value > 0.08f) pressAnimation.value * 0.84f else 0.0f, 16.0)

        val baseBackground = CompStyleResolver.resolveControlBase(variant, paletteColors, accentColors)
        val hoverBackground = CompStyleResolver.resolveControlHover(variant, paletteColors, accentColors)

        var background = ColorUtils.interpolateColor(baseBackground, hoverBackground, hoverAnimation.value.toDouble())
        if (pressAnimation.value > 0.08f) {
            background = ColorUtils.darken(background, pressAnimation.value * 0.16f)
        }

        if (variant == CompControlVariant.PRIMARY) {
            val gradientStart = ColorUtils.applyAlpha(
                accentColors.getColor1(),
                if (enabled) (170 + (hoverAnimation.value * 50f).toInt()) else 98
            )
            val gradientEnd = ColorUtils.applyAlpha(
                accentColors.getColor2(),
                if (enabled) (180 + (hoverAnimation.value * 50f).toInt()) else 98
            )
            nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), getRadius(), background)
            nvgInstance.drawGradientRoundedRect(getX(), getY(), getWidth(), getHeight(), getRadius(), gradientStart, gradientEnd)
        } else {
            nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), getRadius(), background)
        }

        val outlineIdle = ColorUtils.applyAlpha(paletteColors.getFontColor(ColorType.NORMAL), 28)
        val outlineHover = ColorUtils.applyAlpha(accentColors.getColor1(), 96)
        var outlineColor = ColorUtils.interpolateColor(outlineIdle, outlineHover, hoverAnimation.value.toDouble())
        if (!enabled) {
            outlineColor = ColorUtils.applyAlpha(outlineColor, 96)
        }
        nvgInstance.drawOutlineRoundedRect(getX(), getY(), getWidth(), getHeight(), getRadius(), 1f, outlineColor)

        getText()?.let {
            val baseTextColor = getTextColor() ?: if (enabled) {
                CompStyleResolver.resolveControlText(variant, paletteColors)
            } else {
                paletteColors.getFontColor(ColorType.NORMAL, 150)
            }
            val drawTextColor = if (enabled) {
                ColorUtils.interpolateColor(baseTextColor, Color.WHITE, (hoverAnimation.value * 0.22f).toDouble())
            } else {
                baseTextColor
            }
            val textHeight = nvgInstance.getTextHeight(it, getFontSize(), Fonts.REGULAR)
            val textY = getY() + getHeight() / 2f - textHeight / 2f
            nvgInstance.drawCenteredText(
                it,
                getX() + getWidth() / 2f,
                textY,
                drawTextColor,
                getFontSize(),
                Fonts.REGULAR
            )
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && isEnabled() && isHoveredInteractive(mouseX, mouseY)) {
            pressAnimation.value = 1.0f
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
