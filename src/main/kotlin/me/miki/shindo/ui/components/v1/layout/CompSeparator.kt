package me.miki.shindo.ui.components.v1.layout

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.components.v1.templates.CompDisplay
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

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
                Comp.setWidth(length)
                Comp.setHeight(thickness)
            }

            Orientation.VERTICAL -> {
                Comp.setWidth(thickness)
                Comp.setHeight(length)
            }
        }
    }

    fun setThickness(thickness: Float): CompSeparator {
        this.thickness = thickness
        when (orientation) {
            Orientation.HORIZONTAL -> Comp.setHeight(thickness)
            Orientation.VERTICAL -> Comp.setWidth(thickness)
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
                val length = Comp.getHeight()
                Comp.setWidth(length)
                Comp.setHeight(thickness)
            }

            Orientation.VERTICAL -> {
                val length = Comp.getWidth()
                Comp.setWidth(thickness)
                Comp.setHeight(length)
            }
        }
        return this
    }

    override fun drawDisplay(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = Comp.nvg
        val paletteColors = Comp.palette

        val finalColor = color ?: ColorUtils.applyAlpha(
            paletteColors.getBackgroundColor(ColorType.NORMAL),
            150
        )

        if (gradient) {
            val gradColor = gradientColor ?: ColorUtils.applyAlpha(finalColor, 0)
            when (orientation) {
                Orientation.HORIZONTAL -> {
                    nvgInstance.drawHorizontalGradientRect(
                        Comp.getX(),
                        Comp.getY(),
                        Comp.getWidth(),
                        Comp.getHeight(),
                        gradColor,
                        finalColor
                    )
                }

                Orientation.VERTICAL -> {
                    nvgInstance.drawVerticalGradientRect(
                        Comp.getX(),
                        Comp.getY(),
                        Comp.getWidth(),
                        Comp.getHeight(),
                        gradColor,
                        finalColor
                    )
                }
            }
        } else {
            nvgInstance.drawRect(Comp.getX(), Comp.getY(), Comp.getWidth(), Comp.getHeight(), finalColor)
        }
    }
}
