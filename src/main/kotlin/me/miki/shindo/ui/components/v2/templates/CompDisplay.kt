package me.miki.shindo.ui.components.v2.templates

import me.miki.shindo.ui.components.v2.Component

abstract class CompDisplay(
    x: Float = 0f,
    y: Float = 0f,
) : Component(x, y) {
    protected abstract fun drawDisplay(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    )

    override fun draw(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        if (!isVisible()) return
        drawDisplay(mouseX, mouseY, partialTicks)
        super.draw(mouseX, mouseY, partialTicks)
    }
}
