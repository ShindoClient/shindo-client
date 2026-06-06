package com.shindoclient.shindo.ui.components.v2.layout

import com.shindoclient.shindo.management.color.palette.ColorType
import com.shindoclient.shindo.ui.components.v2.Component
import com.shindoclient.shindo.ui.components.v2.templates.CompScrollable
import com.shindoclient.shindo.utils.ColorUtils

open class CompScrollableWithChildren(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f,
) : CompScrollable(x, y, width, height) {
    override fun drawScrollableContent(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        for (child in getChildren()) {
            child.draw(mouseX, mouseY, partialTicks)
        }
    }

    override fun draw(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        if (!isVisible()) return

        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent
        val scrollbarWidth = 6f
        val scrollbarEnabled = true
        val contentHeight = getContentHeight()

        nvgInstance.save()
        nvgInstance.scissor(getX(), getY(), getWidth(), getHeight())

        nvgInstance.save()
        nvgInstance.translate(0f, -getScrollY())

        drawScrollableContent(mouseX, mouseY, partialTicks)

        nvgInstance.restore()

        val maxScroll = (contentHeight - getHeight()).coerceAtLeast(0f)
        if (scrollbarEnabled && contentHeight > getHeight()) {
            val scrollbarX = getX() + getWidth() - scrollbarWidth - 4f
            val scrollbarY = getY() + 4f
            val scrollbarHeight = getHeight() - 8f

            val trackColor = ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.NORMAL), 130)
            nvgInstance.drawRoundedRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 2f, trackColor)

            val visibleRatio = (getHeight() / contentHeight).coerceIn(0.1f, 1f)
            val handleHeight = scrollbarHeight * visibleRatio
            val handleY = scrollbarY + (scrollbarHeight - handleHeight) * (getScrollY() / maxScroll.coerceAtLeast(1f))

            val handleColor = ColorUtils.applyAlpha(accentColors.getColor1(), 190)
            nvgInstance.drawRoundedRect(scrollbarX, handleY, scrollbarWidth, handleHeight, 2f, handleColor)
        }

        nvgInstance.restore()
        // Não chama super.draw para evitar desenhar filhos duas vezes
    }
}
