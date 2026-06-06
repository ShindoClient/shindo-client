package com.shindoclient.shindo.ui.components.v2.buttons

import com.shindoclient.shindo.management.color.palette.ColorType
import com.shindoclient.shindo.management.nanovg.font.Fonts
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation
import com.shindoclient.shindo.ui.components.v2.templates.CompControlTemplate
import com.shindoclient.shindo.utils.ColorUtils
import java.awt.Color

class CompIconButton : CompControlTemplate {
    private val iconSupplier: () -> String?
    private var enabledSupplier: (() -> Boolean)? = null

    private val hoverAnimation = SimpleAnimation()
    private val pressAnimation = SimpleAnimation()

    private var radius: Float = 6f
    private var iconSize: Float = 12f
    private var fontSize: Float = 12f
    private var overrideBackground: Color? = null
    private var iconColorSupplier: (() -> Color)? = null

    constructor(x: Float, y: Float, size: Float, iconSupplier: () -> String?) : super(x, y) {
        this.iconSupplier = iconSupplier
        setWidth(size)
        setHeight(size)
    }

    constructor(size: Float, iconSupplier: () -> String?) : this(0f, 0f, size, iconSupplier)

    fun onClick(runnable: () -> Unit): CompIconButton {
        this.onClick = runnable
        return this
    }

    fun enabledWhen(enabledSupplier: (() -> Boolean)?): CompIconButton {
        this.enabledSupplier = enabledSupplier
        return this
    }

    override fun setRadius(radius: Float): CompIconButton {
        this.radius = radius
        return this
    }

    fun setIconSize(iconSize: Float): CompIconButton {
        this.iconSize = iconSize
        return this
    }

    override fun setFontSize(fontSize: Float): CompIconButton {
        this.fontSize = fontSize
        return this
    }

    fun setOverrideBackground(overrideBackground: Color?): CompIconButton {
        this.overrideBackground = overrideBackground
        return this
    }

    fun setIconColorSupplier(iconColorSupplier: (() -> Color)?): CompIconButton {
        this.iconColorSupplier = iconColorSupplier
        return this
    }

    override fun isEnabled(): Boolean = enabledSupplier?.invoke() ?: true

    override fun drawInteractive(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        hovered: Boolean,
    ) {
        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent
        val enabled = isEnabled()

        hoverAnimation.setAnimation(if (hovered && enabled) 1.0f else 0.0f, 16.0)
        pressAnimation.setAnimation(
            if (pressAnimation.getValue() > 0.08f) pressAnimation.getValue() * 0.82f else 0.0f,
            16.0,
        )

        var drawBackground =
            ColorUtils.interpolateColor(
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 108),
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 145),
                hoverAnimation.getValue().toDouble(),
            )
        if (pressAnimation.getValue() > 0.08f) {
            drawBackground = ColorUtils.darken(drawBackground, pressAnimation.getValue() * 0.18f)
        }
        if (!enabled) {
            drawBackground = ColorUtils.applyAlpha(drawBackground, 118)
        }

        val outlineIdle = ColorUtils.applyAlpha(paletteColors.getFontColor(ColorType.NORMAL), 26)
        val outlineHover = ColorUtils.applyAlpha(accentColors.getColor1(), 92)
        var outlineColor = ColorUtils.interpolateColor(outlineIdle, outlineHover, hoverAnimation.getValue().toDouble())
        if (!enabled) {
            outlineColor = ColorUtils.applyAlpha(outlineColor, 94)
        }

        nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), radius, drawBackground)
        nvgInstance.drawOutlineRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            radius,
            1f,
            outlineColor,
        )

        val icon = iconSupplier.invoke()
        if (icon != null) {
            val baseIconColor = iconColorSupplier?.invoke() ?: paletteColors.getFontColor(ColorType.DARK)
            val hoverIconColor =
                if (iconColorSupplier != null) {
                    ColorUtils.interpolateColor(baseIconColor, Color.WHITE, 0.2)
                } else {
                    ColorUtils.lighten(baseIconColor, 0.16f)
                }
            var iconColor =
                ColorUtils.interpolateColor(baseIconColor, hoverIconColor, hoverAnimation.getValue().toDouble())
            if (!enabled) {
                iconColor = ColorUtils.applyAlpha(iconColor, 132)
            }

            val drawSize = iconSize.coerceAtLeast(fontSize)
            val textHeight = nvgInstance.getTextHeight(icon, drawSize, Fonts.LUCIDE)
            val textWidth = nvgInstance.getTextWidth(icon, drawSize, Fonts.LUCIDE)
            val iconX = getX() + getWidth() / 2f - textWidth / 2f
            val iconY = getY() + getHeight() / 2f - textHeight / 2f

            nvgInstance.drawText(icon, iconX, iconY, iconColor, drawSize, Fonts.LUCIDE)
        }
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton == 0 && isEnabled() && super.isHoveredInteractive(mouseX, mouseY)) {
            pressAnimation.setValue(1.0f)
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
