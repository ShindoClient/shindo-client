package me.miki.shindo.ui.comp.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.animation.value.ColorAnimation
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.comp.templates.CompInteractive
import me.miki.shindo.utils.ColorUtils

/**
 * Toggle button para addons, sem depender de BooleanSetting.
 */
class CompAddonToggleButton(
    x: Float,
    y: Float,
    scale: Float,
    initialValue: Boolean,
    private val onChange: (Boolean) -> Unit
) : CompInteractive(x, y) {

    private val opacityAnimation = SimpleAnimation()
    private val toggleAnimation = SimpleAnimation()
    private val circleAnimation = ColorAnimation()

    var value: Boolean = initialValue
        set(v) {
            if (field != v) {
                field = v
                onChange(v)
            }
        }

    init {
        setScale(scale)
        toggleAnimation.value = if (initialValue) 20.5f else 2.5f
        circleAnimation.setColor(if (initialValue) java.awt.Color.WHITE else palette.getBackgroundColor(ColorType.DARK))
    }

    fun setScale(scale: Float) {
        super.setWidth(34f * scale)
        super.setHeight(16f * scale)
    }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val nvgInstance = nvg
        val accentColor = accent
        val paletteColors = palette
        val scale = getHeight() / 16f
        val circle = 11f * scale

        opacityAnimation.setAnimation(if (value) 1.0f else 0.0f, 14.0)
        toggleAnimation.setAnimation(if (value) 20.5f else 2.5f, 14.0)

        nvgInstance.drawRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            7f * scale,
            paletteColors.getBackgroundColor(ColorType.NORMAL)
        )
        nvgInstance.drawGradientRoundedRect(
            getX(), getY(), getWidth(), getHeight(), 7.4f * scale,
            ColorUtils.applyAlpha(accentColor.getColor1(), (opacityAnimation.value * 255).toInt()),
            ColorUtils.applyAlpha(accentColor.getColor2(), (opacityAnimation.value * 255).toInt())
        )
        nvgInstance.drawRoundedRect(
            getX() + toggleAnimation.value * scale,
            getY() + 2.5f * scale,
            circle, circle, circle / 2f,
            circleAnimation.getColor(
                if (value) java.awt.Color.WHITE else paletteColors.getBackgroundColor(ColorType.DARK),
                16
            )
        )
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            value = !value
        }
    }
}
