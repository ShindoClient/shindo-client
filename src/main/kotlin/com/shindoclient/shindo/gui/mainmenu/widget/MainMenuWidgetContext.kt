package com.shindoclient.shindo.gui.mainmenu.widget

import com.shindoclient.shindo.management.nanovg.NanoVGManager

data class MainMenuWidgetContext(
    val nvg: NanoVGManager,
    val mouseX: Int,
    val mouseY: Int,
    val sw: Float,
    val sh: Float,
    val anim: Float,
    val partialTicks: Float,
)
