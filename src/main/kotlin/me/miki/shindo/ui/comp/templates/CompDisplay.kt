package me.miki.shindo.ui.comp.templates

import me.miki.shindo.ui.comp.Comp
abstract class CompDisplay(
        x: Float = 0f,
        y: Float = 0f
) : Comp(x, y) {
    protected abstract fun drawDisplay(mouseX: Int, mouseY: Int, partialTicks: Float)

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return
        drawDisplay(mouseX, mouseY, partialTicks)
        super.draw(mouseX, mouseY, partialTicks)
    }

}
