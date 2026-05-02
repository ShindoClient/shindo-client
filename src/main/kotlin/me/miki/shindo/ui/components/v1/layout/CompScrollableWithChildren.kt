package me.miki.shindo.ui.components.v1.layout

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.ui.components.v1.templates.CompScrollable
import me.miki.shindo.utils.ColorUtils

/**
 * CompScrollable que desenha seus filhos (children) dentro da área scrollável.
 * Sobrescreve draw para não chamar super.draw (evita duplicar filhos).
 */
open class CompScrollableWithChildren(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : CompScrollable(x, y, width, height) {

    override fun drawScrollableContent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        for (child in Comp.getChildren()) {
            if (child is Comp) {
                child.draw(mouseX, mouseY, partialTicks)
            }
        }
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!Comp.isVisible()) return

        val nvgInstance = Comp.nvg
        val paletteColors = Comp.palette
        val accentColors = Comp.accent
        val scrollbarWidth = 6f
        val scrollbarEnabled = true
        val contentHeight = getContentHeight()

        nvgInstance.save()
        nvgInstance.scissor(Comp.getX(), Comp.getY(), Comp.getWidth(), Comp.getHeight())

        nvgInstance.save()
        nvgInstance.translate(0f, -getScrollY())

        drawScrollableContent(mouseX, mouseY, partialTicks)

        nvgInstance.restore()

        val maxScroll = (contentHeight - Comp.getHeight()).coerceAtLeast(0f)
        if (scrollbarEnabled && contentHeight > Comp.getHeight()) {
            val scrollbarX = Comp.getX() + Comp.getWidth() - scrollbarWidth - 4f
            val scrollbarY = Comp.getY() + 4f
            val scrollbarHeight = Comp.getHeight() - 8f

            val trackColor = ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.NORMAL), 130)
            nvgInstance.drawRoundedRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 2f, trackColor)

            val visibleRatio = (Comp.getHeight() / contentHeight).coerceIn(0.1f, 1f)
            val handleHeight = scrollbarHeight * visibleRatio
            val handleY = scrollbarY + (scrollbarHeight - handleHeight) * (getScrollY() / maxScroll.coerceAtLeast(1f))

            val handleColor = ColorUtils.applyAlpha(accentColors.getColor1(), 190)
            nvgInstance.drawRoundedRect(scrollbarX, handleY, scrollbarWidth, handleHeight, 2f, handleColor)
        }

        nvgInstance.restore()
        // Não chama super.draw para evitar desenhar filhos duas vezes
    }
}
