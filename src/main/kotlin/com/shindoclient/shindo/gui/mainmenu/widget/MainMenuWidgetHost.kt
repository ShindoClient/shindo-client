package com.shindoclient.shindo.gui.mainmenu.widget

import com.shindoclient.shindo.management.nanovg.NanoVGManager

class MainMenuWidgetHost {
    private val widgets: MutableList<MainMenuWidget> = mutableListOf()

    fun register(widget: MainMenuWidget) {
        widgets.add(widget)
    }

    fun unregister(widget: MainMenuWidget) {
        widgets.remove(widget)
    }

    fun getWidgets(): List<MainMenuWidget> = widgets

    fun onSceneInit() {
        for (w in widgets) {
            if (w.enabled) w.onSceneInit()
        }
    }

    fun draw(
        nvg: NanoVGManager,
        mouseX: Int,
        mouseY: Int,
        sw: Float,
        sh: Float,
        anim: Float,
        partialTicks: Float,
    ) {
        val ctx = MainMenuWidgetContext(nvg, mouseX, mouseY, sw, sh, anim, partialTicks)
        for (w in widgets) {
            if (!w.enabled) continue
            val (x, y) = resolveOrigin(w, sw, sh)
            w.draw(ctx, x, y)
        }
    }

    fun mouseClicked(
        nvg: NanoVGManager,
        mouseX: Int,
        mouseY: Int,
        sw: Float,
        sh: Float,
        anim: Float,
        partialTicks: Float,
        mouseButton: Int,
    ): Boolean {
        val ctx = MainMenuWidgetContext(nvg, mouseX, mouseY, sw, sh, anim, partialTicks)
        for (w in widgets.asReversed()) {
            if (!w.enabled) continue
            val (x, y) = resolveOrigin(w, sw, sh)
            if (w.mouseClicked(ctx, x, y, mouseButton)) return true
        }
        return false
    }

    fun mouseScrolled(
        nvg: NanoVGManager,
        mouseX: Int,
        mouseY: Int,
        sw: Float,
        sh: Float,
        anim: Float,
        partialTicks: Float,
        amount: Int,
    ): Boolean {
        val ctx = MainMenuWidgetContext(nvg, mouseX, mouseY, sw, sh, anim, partialTicks)
        for (w in widgets.asReversed()) {
            if (!w.enabled) continue
            val (x, y) = resolveOrigin(w, sw, sh)
            if (w.mouseScrolled(ctx, x, y, amount)) return true
        }
        return false
    }

    fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ): Boolean {
        for (w in widgets.asReversed()) {
            if (!w.enabled) continue
            if (w.keyTyped(typedChar, keyCode)) return true
        }
        return false
    }

    private fun resolveOrigin(
        widget: MainMenuWidget,
        sw: Float,
        sh: Float,
    ): Pair<Float, Float> {
        val p = widget.anchorPadding
        val w = widget.width
        val h = widget.height
        return when (widget.anchor) {
            WidgetAnchor.TOP_LEFT -> Pair(p, p)
            WidgetAnchor.TOP_RIGHT -> Pair(sw - p - w, p)
            WidgetAnchor.BOTTOM_LEFT -> Pair(p, sh - p - h)
            WidgetAnchor.BOTTOM_RIGHT -> Pair(sw - p - w, sh - p - h)
            WidgetAnchor.NONE -> Pair(0f, 0f)
        }
    }
}
