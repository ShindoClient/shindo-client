package me.miki.shindo.ui.comp.overlays

import me.miki.shindo.ui.comp.style.CompSurfaceVariant
import me.miki.shindo.ui.comp.templates.CompSurfaceTemplate

open class CompOverlayPanel(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : CompSurfaceTemplate(x, y, width, height) {

    init {
        setSurfaceVariant(CompSurfaceVariant.OVERLAY)
        setRadius(12f)
        setShadowStrength(8)
    }
}
