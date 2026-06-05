package me.miki.shindo.management.addons.bridge.render

import me.miki.addon.api.graphics.NanoVG
import me.miki.shindo.management.nanovg.NanoVGManager
import java.awt.Color

class AddonNanoVGBridge(
    private val nvgManager: NanoVGManager,
) : NanoVG {
    private val nvg: Long get() = nvgManager.getContext()

    override fun rgba(r: Int, g: Int, b: Int, a: Int): Int =
        (a shl 24) or (r shl 16) or (g shl 8) or b

    override fun rgba(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or (alpha shl 24)

    override fun rgb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    override fun drawRect(x: Float, y: Float, w: Float, h: Float, color: Int) =
        nvgManager.drawRect(x, y, w, h, color)

    override fun drawRoundedRect(x: Float, y: Float, w: Float, h: Float, radius: Float, color: Int) =
        nvgManager.drawRoundedRect(x, y, w, h, radius, color)

    override fun drawRoundedRectVarying(
        x: Float, y: Float, w: Float, h: Float,
        topLeft: Float, topRight: Float, bottomLeft: Float, bottomRight: Float,
        color: Int,
    ) = nvgManager.drawRoundedRectVarying(x, y, w, h, topLeft, topRight, bottomLeft, bottomRight, color)

    override fun drawCircle(cx: Float, cy: Float, radius: Float, color: Int) =
        nvgManager.drawCircle(cx, cy, radius, color)

    override fun drawOutlineRoundedRect(
        x: Float, y: Float, w: Float, h: Float, radius: Float, strokeWidth: Float, color: Int,
    ) = nvgManager.drawOutlineRoundedRect(x, y, w, h, radius, strokeWidth, color)

    override fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, strokeWidth: Float, color: Int) =
        nvgManager.drawLine(x1, y1, x2, y2, strokeWidth, Color(color, true))

    override fun drawArc(
        cx: Float, cy: Float, radius: Float,
        startAngleDeg: Float, endAngleDeg: Float,
        strokeWidth: Float, color: Int,
    ) = nvgManager.drawArc(cx, cy, radius, startAngleDeg, endAngleDeg, strokeWidth, color)

    override fun drawVerticalGradientRect(
        x: Float, y: Float, w: Float, h: Float, colorTop: Int, colorBottom: Int,
    ) = nvgManager.drawVerticalGradientRect(x, y, w, h, colorTop, colorBottom)

    override fun drawHorizontalGradientRect(
        x: Float, y: Float, w: Float, h: Float, colorLeft: Int, colorRight: Int,
    ) = nvgManager.drawHorizontalGradientRect(x, y, w, h, colorLeft, colorRight)

    override fun drawGradientRoundedRect(
        x: Float, y: Float, w: Float, h: Float, radius: Float, color1: Int, color2: Int,
    ) = nvgManager.drawGradientRoundedRect(x, y, w, h, radius, color1, color2)

    override fun drawText(
        text: String, x: Float, y: Float, color: Int, fontSize: Float, font: String,
    ) {
        val yPos = y + fontSize / 2f
        val nvgColor = nvgManager.getColor(color)
        val ctx = nvg
        org.lwjgl.nanovg.NanoVG.nvgBeginPath(ctx)
        org.lwjgl.nanovg.NanoVG.nvgFontSize(ctx, fontSize)
        org.lwjgl.nanovg.NanoVG.nvgFontFace(ctx, font)
        org.lwjgl.nanovg.NanoVG.nvgTextAlign(ctx, org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT or org.lwjgl.nanovg.NanoVG.NVG_ALIGN_MIDDLE)
        org.lwjgl.nanovg.NanoVG.nvgFillColor(ctx, nvgColor)
        org.lwjgl.nanovg.NanoVG.nvgText(ctx, x, yPos, text)
    }

    override fun drawCenteredText(
        text: String, cx: Float, cy: Float, color: Int, fontSize: Float, font: String,
    ) {
        val textWidth = getTextWidth(text, fontSize, font)
        drawText(text, cx - textWidth / 2f, cy, color, fontSize, font)
    }

    override fun drawTextBox(
        text: String, x: Float, y: Float, maxWidth: Float, color: Int, fontSize: Float, font: String,
    ) {
        val yPos = y + fontSize / 2f
        val nvgColor = nvgManager.getColor(color)
        val ctx = nvg
        org.lwjgl.nanovg.NanoVG.nvgBeginPath(ctx)
        org.lwjgl.nanovg.NanoVG.nvgFontSize(ctx, fontSize)
        org.lwjgl.nanovg.NanoVG.nvgFontFace(ctx, font)
        org.lwjgl.nanovg.NanoVG.nvgTextAlign(ctx, org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT or org.lwjgl.nanovg.NanoVG.NVG_ALIGN_MIDDLE)
        org.lwjgl.nanovg.NanoVG.nvgFillColor(ctx, nvgColor)
        org.lwjgl.nanovg.NanoVG.nvgTextBox(ctx, x, yPos, maxWidth, text)
    }

    override fun getTextWidth(text: String, fontSize: Float, font: String): Float {
        val ctx = nvg
        org.lwjgl.nanovg.NanoVG.nvgFontSize(ctx, fontSize)
        org.lwjgl.nanovg.NanoVG.nvgFontFace(ctx, font)
        val bounds = FloatArray(4)
        org.lwjgl.nanovg.NanoVG.nvgTextBounds(ctx, 0f, 0f, text, bounds)
        org.lwjgl.nanovg.NanoVG.nvgTextAlign(ctx, org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT or org.lwjgl.nanovg.NanoVG.NVG_ALIGN_MIDDLE)
        return bounds[2] - bounds[0]
    }

    override fun getTextHeight(text: String, fontSize: Float, font: String): Float {
        val ctx = nvg
        org.lwjgl.nanovg.NanoVG.nvgFontSize(ctx, fontSize)
        org.lwjgl.nanovg.NanoVG.nvgFontFace(ctx, font)
        val bounds = FloatArray(4)
        org.lwjgl.nanovg.NanoVG.nvgTextBounds(ctx, 0f, 0f, text, bounds)
        return bounds[3] - bounds[1]
    }

    override fun save() = nvgManager.save()
    override fun restore() = nvgManager.restore()
    override fun translate(dx: Float, dy: Float) = nvgManager.translate(dx, dy)

    override fun scale(sx: Float, sy: Float) {
        org.lwjgl.nanovg.NanoVG.nvgScale(nvg, sx, sy)
    }

    override fun rotate(angleRadians: Float) =
        org.lwjgl.nanovg.NanoVG.nvgRotate(nvg, angleRadians)

    override fun setAlpha(alpha: Float) = nvgManager.setAlpha(alpha)
    override fun scissor(x: Float, y: Float, w: Float, h: Float) = nvgManager.scissor(x, y, w, h)
    override fun resetScissor() = nvgManager.resetScissor()
}
