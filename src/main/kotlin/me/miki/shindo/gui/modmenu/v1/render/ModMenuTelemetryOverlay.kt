package me.miki.shindo.gui.modmenu.v1.render

import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import java.awt.Color

/**
 * Lightweight UI telemetry overlay for ModMenu profiling/debug sessions.
 */
class ModMenuTelemetryOverlay {

    private var frameStartNanos = 0L
    private var lastFrameMs = 0f
    private var smoothedFrameMs = 0f
    private var enabled = false

    fun beginFrame() {
        frameStartNanos = System.nanoTime()
    }

    fun endFrame() {
        if (frameStartNanos <= 0L) {
            return
        }

        val durationNs = System.nanoTime() - frameStartNanos
        lastFrameMs = durationNs / 1_000_000f
        smoothedFrameMs = if (smoothedFrameMs == 0f) {
            lastFrameMs
        } else {
            (smoothedFrameMs * 0.88f) + (lastFrameMs * 0.12f)
        }
    }

    fun toggle() {
        enabled = !enabled
    }

    fun isEnabled(): Boolean = enabled

    fun draw(nvg: NanoVGManager, palette: ColorPalette, menuX: Float, menuY: Float, menuWidth: Float) {
        if (!enabled) {
            return
        }

        val boxWidth = 136f
        val boxHeight = 48f
        val x = menuX + menuWidth - boxWidth - 8f
        val y = menuY + 8f

        nvg.drawRoundedRect(x, y, boxWidth, boxHeight, 6f, Color(0, 0, 0, 120))
        nvg.drawText("UI frame", x + 8f, y + 10f, palette.getFontColor(ColorType.NORMAL), 8f, Fonts.REGULAR)
        nvg.drawText(
            String.format("%.2f ms (avg %.2f)", lastFrameMs, smoothedFrameMs),
            x + 8f,
            y + 22f,
            palette.getFontColor(ColorType.DARK),
            8.5f,
            Fonts.MEDIUM
        )
        val clipDebugStatus = if (ModMenuClipCoordinator.isDebugOverlayEnabled()) {
            "on (" + ModMenuClipCoordinator.getCapturedClipCount() + ")"
        } else {
            "off"
        }
        nvg.drawText(
            "Clip debug: $clipDebugStatus",
            x + 8f,
            y + 34f,
            palette.getFontColor(ColorType.NORMAL),
            8f,
            Fonts.REGULAR
        )
    }
}
