package me.miki.shindo.gui.mainmenu.widget

import me.miki.shindo.management.nanovg.NanoVGManager

data class MainMenuWidgetContext(
    val nvg: NanoVGManager,
    val mouseX: Int,
    val mouseY: Int,
    val sw: Float,
    val sh: Float,
    val anim: Float,
    val partialTicks: Float,
)
