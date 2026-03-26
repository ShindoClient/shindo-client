package me.miki.shindo.ui.comp.templates

import me.miki.shindo.ui.comp.style.CompStyleResolver
import java.awt.Color

open class CompSurfaceTemplate(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : CompPanel(x, y, width, height) {

    private var autoBorder = true

    fun setAutoBorder(autoBorder: Boolean): CompSurfaceTemplate {
        this.autoBorder = autoBorder
        return this
    }

    override fun beforeDrawPanel(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (autoBorder) {
            setBorder(1f, CompStyleResolver.resolveSurfaceBorder(getSurfaceVariant(), palette, accent))
        }
    }

    override fun resolveDefaultBackground(): Color {
        return CompStyleResolver.resolveSurfaceBackground(getSurfaceVariant(), palette, accent)
    }
}
