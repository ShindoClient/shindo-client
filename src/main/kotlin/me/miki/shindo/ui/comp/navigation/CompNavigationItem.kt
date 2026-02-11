package me.miki.shindo.ui.comp.navigation

import me.miki.shindo.ui.comp.style.CompControlVariant
import me.miki.shindo.ui.comp.templates.CompControlTemplate

open class CompNavigationItem(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : CompControlTemplate(x, y, width, height) {

    init {
        setVariant(CompControlVariant.GHOST)
    }
}
