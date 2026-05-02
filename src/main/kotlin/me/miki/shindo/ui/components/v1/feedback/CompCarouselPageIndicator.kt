package me.miki.shindo.ui.components.v1.feedback

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.ColorUtils
import kotlin.math.max

class CompCarouselPageIndicator : Comp() {

    private var pageCount: Int = 0
    private var selectedIndex: Int = 0
    private var dotSize: Float = 5f
    private var dotSpacing: Float = 6f

    fun setPageCount(count: Int): CompCarouselPageIndicator {
        pageCount = max(0, count)
        if (selectedIndex >= pageCount) {
            selectedIndex = max(0, pageCount - 1)
        }
        return this
    }

    fun setSelectedIndex(index: Int): CompCarouselPageIndicator {
        if (pageCount <= 0) {
            selectedIndex = 0
            return this
        }
        selectedIndex = index.coerceIn(0, pageCount - 1)
        return this
    }

    fun setDotMetrics(size: Float, spacing: Float): CompCarouselPageIndicator {
        dotSize = max(2f, size)
        dotSpacing = max(2f, spacing)
        return this
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible() || pageCount <= 1 || getWidth() <= 0f || getHeight() <= 0f) {
            return
        }

        val totalWidth = pageCount * dotSize + (pageCount - 1) * dotSpacing
        val startX = getX() + (getWidth() - totalWidth) / 2f
        val centerY = getY() + getHeight() / 2f
        val normalColor = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 120)
        val activeColor = ColorUtils.applyAlpha(accent.getColor1(), 225)

        for (index in 0 until pageCount) {
            val size = if (index == selectedIndex) dotSize + 1f else dotSize
            val radius = size / 2f
            val dotX = startX + index * (dotSize + dotSpacing) + (dotSize - size) / 2f
            val dotY = centerY - radius
            nvg.drawRoundedRect(
                dotX,
                dotY,
                size,
                size,
                radius,
                if (index == selectedIndex) activeColor else normalColor
            )
        }
    }
}

