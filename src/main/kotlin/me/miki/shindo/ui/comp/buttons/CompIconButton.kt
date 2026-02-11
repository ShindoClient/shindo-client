package me.miki.shindo.ui.comp.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.style.CompControlVariant
import me.miki.shindo.ui.comp.templates.CompControlTemplate
import me.miki.shindo.utils.ColorUtils
import java.awt.Color
class CompIconButton : CompControlTemplate {
    private val iconSupplier: () -> String?
    private var enabledSupplier: (() -> Boolean)? = null

    private var radius: Float = 6f
    private var iconSize: Float = 12f
    private var fontSize: Float = 12f
    private var overrideBackground: Color? = null
    private var iconColorSupplier: (() -> Color)? = null

    constructor(x: Float, y: Float, size: Float, iconSupplier: () -> String?) : super(x, y) {
        this.iconSupplier = iconSupplier
        setWidth(size)
        setHeight(size)
        setVariant(CompControlVariant.GHOST)
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

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent
        val enabled = isEnabled()

        val baseBackground = overrideBackground ?: ColorUtils.applyAlpha(
                paletteColors.getBackgroundColor(ColorType.DARK),
                if (enabled) 190 else 120
        )

        val start = ColorUtils.applyAlpha(
                accentColors.getColor1(),
                if (enabled) if (hovered) 210 else 180 else 90
        )
        val end = ColorUtils.applyAlpha(
                accentColors.getColor2(),
                if (enabled) if (hovered) 210 else 180 else 90
        )

        nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), radius, baseBackground)
        nvgInstance.drawGradientRoundedRect(getX(), getY(), getWidth(), getHeight(), radius, start, end)

        val iconColor = iconColorSupplier?.invoke() ?: Color(255, 255, 255, if (enabled) 255 else 155)
        val icon = iconSupplier.invoke()
        if (icon != null) {
            val centerX = getX() + getWidth() / 2f
            val centerY = getY() + getHeight() / 2f - nvgInstance.getTextHeight(icon, fontSize, Fonts.LEGACYICON) / 2f
            nvgInstance.drawText(
                    icon,
                    centerX - nvgInstance.getTextWidth(icon, fontSize, Fonts.LEGACYICON) / 2f,
                    centerY,
                    iconColor,
                    fontSize,
                    Fonts.LEGACYICON
            )
        }
    }
}
