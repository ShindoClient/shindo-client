package me.miki.shindo.ui.comp.layout

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.comp.templates.CompDisplay
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

/**
 * Separador visual horizontal ou vertical.
 * Útil para dividir seções de UI.
 */
open class CompSeparator(
    x: Float = 0f,
    y: Float = 0f,
    length: Float = 100f,
    orientation: Orientation = Orientation.HORIZONTAL
) : CompDisplay(x, y) {

    enum class Orientation {
        HORIZONTAL, VERTICAL
    }

    private var orientation: Orientation = orientation
    private var thickness: Float = 1f
    private var color: Color? = null
    private var gradient: Boolean = false
    private var gradientColor: Color? = null

    init {
        when (orientation) {
            Orientation.HORIZONTAL -> {
                setWidth(length)
                setHeight(thickness)
            }
            Orientation.VERTICAL -> {
                setWidth(thickness)
                setHeight(length)
            }
        }
    }

    fun setThickness(thickness: Float): CompSeparator {
        this.thickness = thickness
        when (orientation) {
            Orientation.HORIZONTAL -> setHeight(thickness)
            Orientation.VERTICAL -> setWidth(thickness)
        }
        return this
    }

    fun setColor(color: Color?): CompSeparator {
        this.color = color
        return this
    }

    fun setGradient(enabled: Boolean, gradientColor: Color? = null): CompSeparator {
        this.gradient = enabled
        this.gradientColor = gradientColor
        return this
    }

    fun setOrientation(orientation: Orientation): CompSeparator {
        this.orientation = orientation
        when (orientation) {
            Orientation.HORIZONTAL -> {
                val length = getHeight()
                setWidth(length)
                setHeight(thickness)
            }
            Orientation.VERTICAL -> {
                val length = getWidth()
                setWidth(thickness)
                setHeight(length)
            }
        }
        return this
    }

    override fun drawDisplay(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val paletteColors = palette

        val finalColor = color ?: ColorUtils.applyAlpha(
            paletteColors.getBackgroundColor(ColorType.NORMAL),
            150
        )

        if (gradient) {
            val gradColor = gradientColor ?: ColorUtils.applyAlpha(finalColor, 0)
            when (orientation) {
                Orientation.HORIZONTAL -> {
                    nvgInstance.drawHorizontalGradientRect(
                        getX(),
                        getY(),
                        getWidth(),
                        getHeight(),
                        gradColor,
                        finalColor
                    )
                }
                Orientation.VERTICAL -> {
                    nvgInstance.drawVerticalGradientRect(
                        getX(),
                        getY(),
                        getWidth(),
                        getHeight(),
                        gradColor,
                        finalColor
                    )
                }
            }
        } else {
            nvgInstance.drawRect(getX(), getY(), getWidth(), getHeight(), finalColor)
        }
    }
}
