package com.shindoclient.shindo.gui.modmenu.v2.category.list

import com.shindoclient.shindo.management.color.AccentColor
import com.shindoclient.shindo.management.color.palette.ColorPalette
import com.shindoclient.shindo.management.nanovg.NanoVGManager

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
    val scrollOffset: Float,
)
