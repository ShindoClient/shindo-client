package me.miki.shindo.ui.components.v2.templates

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
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

        hoverAnimation.setAnimation(if (hovered && isEnabled()) 1.0f else 0.0f, 14.0)
        clickAnimation.setAnimation(
            if (clickAnimation.getValue() > 0.1f) clickAnimation.getValue() * 0.85f else 0.0f,
            16.0
        )

        val finalBg = if (hoverAnimation.getValue() > 0.1f) {
            ColorUtils.interpolateColor(
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210),
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 165),
                hoverAnimation.getValue().toDouble()
            )
        } else {
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210)
        }

        val finalBgWithClick = if (clickAnimation.getValue() > 0.1f) {
            ColorUtils.darken(finalBg, clickAnimation.getValue() * 0.2f)
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

            val textY = getY() + getHeight() / 2f - fontSize / 2f
            nvgInstance.drawCenteredText(
                it,
                getX() + getWidth() / 2f,
                textY,
                paletteColors.getFontColor(ColorType.NORMAL, 150),
                fontSize,
                Fonts.REGULAR
            )
        }
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        clickAnimation.setValue(1.0f)
    }
}
