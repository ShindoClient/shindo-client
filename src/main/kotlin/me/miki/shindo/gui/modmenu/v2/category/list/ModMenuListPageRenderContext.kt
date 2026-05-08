package me.miki.shindo.gui.modmenu.v2.category.list

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.nanovg.NanoVGManager

/**
 * Shared render context passed to ModMenu list-page contract callbacks.
 */
data class ModMenuListPageRenderContext(
    val nvg: NanoVGManager,
    val palette: ColorPalette,
    val accent: AccentColor,
    val mouseX: Int,
    val mouseY: Int,
    val partialTicks: Float,
    val scrollOffset: Float
)

