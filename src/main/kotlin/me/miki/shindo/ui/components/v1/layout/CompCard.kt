package me.miki.shindo.ui.components.v1.layout

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.components.v1.templates.CompPanel
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

class CompCard(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 200f,
    height: Float = 150f
) : CompPanel(x, y, width, height) {

    private var headerHeight: Float = 30f
    private var headerText: String? = null
    private var headerColor: Color? = null
    private var headerTextColor: Color? = null
    private var showHeader: Boolean = false

    init {
        //setSurfaceVariant(CompSurfaceVariant.CARD)
    }

    fun setHeader(text: String, height: Float = 30f): CompCard {
        this.headerText = text
        this.headerHeight = height
        this.showHeader = true
        return this
    }

    fun setHeaderColor(color: Color?): CompCard {
        this.headerColor = color
        return this
    }

    fun setHeaderTextColor(color: Color?): CompCard {
        this.headerTextColor = color
        return this
    }

    fun hideHeader(): CompCard {
        this.showHeader = false
        return this
    }

    override fun drawPanelContent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = Comp.nvg
        val paletteColors = Comp.palette
        val accentColors = Comp.accent

        if (showHeader && headerText != null) {
            val headerBg = headerColor ?: ColorUtils.applyAlpha(accentColors.getColor1(), 180)
            val headerTextColor = this.headerTextColor ?: paletteColors.getFontColor(ColorType.NORMAL)

            nvgInstance.drawRoundedRect(
                Comp.getX(),
                Comp.getY(),
                Comp.getWidth(),
                headerHeight,
                getRadius(),
                headerBg
            )

            nvgInstance.drawText(
                headerText!!,
                Comp.getX() + 10f,
                Comp.getY() + headerHeight / 2f,
                headerTextColor,
                10f,
                Fonts.MEDIUM
            )
        }
    }

    fun getContentY(): Float = if (showHeader) Comp.getY() + headerHeight else Comp.getY()
    fun getContentHeight(): Float = if (showHeader) Comp.getHeight() - headerHeight else Comp.getHeight()
}
