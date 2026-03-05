package me.miki.shindo.gui.modmenu.render

import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import java.awt.Color

/**
 * Centralized clip/scissor helper for ModMenu render paths.
 *
 * Keeps clip setup consistent and prevents leaking state across nested renders.
 */
object ModMenuClipCoordinator {

    /**
     * Logical clip layers used to document clip intent and enable debug overlays.
     */
    enum class ClipLayer {
        CONTENT_VIEWPORT,
        CATEGORY_CONTENT,
        SETTINGS_LIST,
        SETTINGS_SCENE,
        OVERLAY,
        NESTED,
        UNKNOWN
    }

    private data class ClipDebugEntry(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val intersect: Boolean,
        val layer: ClipLayer,
        val tag: String?
    )

    private const val MAX_DEBUG_ENTRIES = 96
    private val debugEntries = ArrayList<ClipDebugEntry>(MAX_DEBUG_ENTRIES)
    private var debugEnabled = false

    fun beginFrame() {
        if (debugEntries.isNotEmpty()) {
            debugEntries.clear()
        }
    }

    fun toggleDebugOverlay(): Boolean {
        debugEnabled = !debugEnabled
        if (!debugEnabled) {
            debugEntries.clear()
        }
        return debugEnabled
    }

    fun isDebugOverlayEnabled(): Boolean = debugEnabled

    fun getCapturedClipCount(): Int = debugEntries.size

    inline fun withClip(
        nvg: NanoVGManager,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        intersect: Boolean = false,
        layer: ClipLayer = ClipLayer.UNKNOWN,
        tag: String? = null,
        block: () -> Unit
    ) {
        val safeWidth = width.coerceAtLeast(0f)
        val safeHeight = height.coerceAtLeast(0f)
        if (safeWidth <= 0f || safeHeight <= 0f) {
            return
        }

        recordDebugEntry(x, y, safeWidth, safeHeight, intersect, layer, tag)

        nvg.withState {
            if (intersect) {
                nvg.intersectScissor(x, y, safeWidth, safeHeight)
            } else {
                nvg.scissor(x, y, safeWidth, safeHeight)
            }
            block()
        }
    }

    inline fun withClipTranslate(
        nvg: NanoVGManager,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        translateX: Float,
        translateY: Float,
        intersect: Boolean = false,
        layer: ClipLayer = ClipLayer.UNKNOWN,
        tag: String? = null,
        block: () -> Unit
    ) {
        withClip(nvg, x, y, width, height, intersect, layer, tag) {
            nvg.translate(translateX, translateY)
            block()
        }
    }

    fun drawDebugOverlay(
        nvg: NanoVGManager,
        originX: Float,
        originY: Float,
        panelWidth: Float
    ) {
        if (!debugEnabled || debugEntries.isEmpty()) {
            return
        }

        for (entry in debugEntries) {
            val color = getLayerColor(entry.layer)
            nvg.drawRect(entry.x, entry.y, entry.width, entry.height, Color(color.red, color.green, color.blue, 24))
            nvg.drawOutlineRoundedRect(
                entry.x,
                entry.y,
                entry.width,
                entry.height,
                3f,
                1f,
                Color(color.red, color.green, color.blue, 170)
            )
        }

        val infoWidth = 208f
        val infoHeight = 44f
        val infoX = originX + panelWidth - infoWidth - 8f
        val infoY = originY + 48f
        nvg.drawRoundedRect(infoX, infoY, infoWidth, infoHeight, 6f, Color(0, 0, 0, 155))
        nvg.drawText("Clip debug", infoX + 8f, infoY + 10f, Color(230, 230, 230, 230), 8f, Fonts.REGULAR)
        nvg.drawText(
            "entries: " + debugEntries.size + " (Ctrl+F10)",
            infoX + 8f,
            infoY + 23f,
            Color(205, 205, 205, 220),
            8f,
            Fonts.REGULAR
        )
        val last = debugEntries[debugEntries.size - 1]
        val lastLabel = if (last.tag.isNullOrEmpty()) last.layer.name else last.tag
        nvg.drawText(
            "last: " + lastLabel + (if (last.intersect) " i" else ""),
            infoX + 8f,
            infoY + 34f,
            Color(185, 185, 185, 210),
            7.8f,
            Fonts.REGULAR
        )
    }

    @PublishedApi
    internal fun recordDebugEntry(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        intersect: Boolean,
        layer: ClipLayer,
        tag: String?
    ) {
        if (!debugEnabled) {
            return
        }

        if (debugEntries.size >= MAX_DEBUG_ENTRIES) {
            debugEntries.removeAt(0)
        }

        debugEntries.add(
            ClipDebugEntry(
                x = x,
                y = y,
                width = width,
                height = height,
                intersect = intersect,
                layer = layer,
                tag = tag
            )
        )
    }

    private fun getLayerColor(layer: ClipLayer): Color {
        return when (layer) {
            ClipLayer.CONTENT_VIEWPORT -> Color(78, 184, 255)
            ClipLayer.CATEGORY_CONTENT -> Color(117, 255, 147)
            ClipLayer.SETTINGS_LIST -> Color(255, 204, 94)
            ClipLayer.SETTINGS_SCENE -> Color(255, 137, 122)
            ClipLayer.OVERLAY -> Color(177, 143, 255)
            ClipLayer.NESTED -> Color(255, 255, 255)
            ClipLayer.UNKNOWN -> Color(194, 194, 194)
        }
    }
}
