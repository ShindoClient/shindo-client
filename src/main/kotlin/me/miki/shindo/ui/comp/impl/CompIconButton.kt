package me.miki.shindo.ui.comp.impl

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color

/**
 * Lightweight icon button that uses the current accent colours. Intended for scenarios
 * like the Mod Menu side bar, small toolbar buttons and quick actions.
 */
class CompIconButton : Comp {
    private val iconSupplier: () -> String?
    private var onClickAction: (() -> Unit)? = null
    private var enabledSupplier: (() -> Boolean)? = null

    private var radius: Float = 6f
    private var iconSize: Float = 12f
    private var fontSize: Float = 12f
    private var overrideBackground: Color? = null
    private var iconColorSupplier: (() -> Color)? = null

    constructor(x: Float, y: Float, size: Float, iconSupplier: () -> String?) : super(x, y) {
        this.iconSupplier = iconSupplier
        setWidth(size);
        setHeight(size);
    }

    constructor(size: Float, iconSupplier: () -> String?) : this(0f, 0f, size, iconSupplier)

    fun onClick(runnable: () -> Unit): CompIconButton {
        this.onClickAction = runnable
        return this
    }

    fun enabledWhen(enabledSupplier: () -> Boolean): CompIconButton {
        this.enabledSupplier = enabledSupplier
        return this
    }

    fun setRadius(radius: Float) { this.radius = radius }
    fun setIconSize(iconSize: Float) { this.iconSize = iconSize }
    fun setFontSize(fontSize: Float) { this.fontSize = fontSize }
    fun setOverrideBackground(overrideBackground: Color?) { this.overrideBackground = overrideBackground }
    fun setIconColorSupplier(iconColorSupplier: (() -> Color)?) { this.iconColorSupplier = iconColorSupplier }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return

        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent

        val enabled = enabledSupplier?.invoke() ?: true
        val hovered = enabled && MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())

        val baseBackground = overrideBackground ?: ColorUtils.applyAlpha(
            paletteColors.getBackgroundColor(ColorType.DARK),
            if (enabled) 190 else 120
        )

        val start = ColorUtils.applyAlpha(accentColors.color1, if (enabled) if (hovered) 210 else 180 else 90)
        val end = ColorUtils.applyAlpha(accentColors.color2, if (enabled) if (hovered) 210 else 180 else 90)

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

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible()) return

        val enabled = enabledSupplier?.invoke() ?: true
        if (enabled && mouseButton == 0 && onClickAction != null && isHovered(mouseX, mouseY)) {
            onClickAction?.invoke()
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
