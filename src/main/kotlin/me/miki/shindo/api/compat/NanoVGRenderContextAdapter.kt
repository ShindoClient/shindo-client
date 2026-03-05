package me.miki.shindo.api.compat

import me.miki.shindo.Shindo
import me.miki.client_api.render.AddonColor
import me.miki.client_api.render.AddonFont
import me.miki.client_api.render.IRenderContext
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.utils.PlayerHeadUtils
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.File

/**
 * Adapta NanoVGManager para IRenderContext do addon-api.
 */
class NanoVGRenderContextAdapter : IRenderContext {

    private val nvg get() = Shindo.getInstance().nanoVGManager
    private val fonts = mapOf(
        AddonFont.REGULAR to Fonts.REGULAR,
        AddonFont.MEDIUM to Fonts.MEDIUM,
        AddonFont.SEMIBOLD to Fonts.SEMIBOLD,
        AddonFont.LEGACYICON to Fonts.LEGACYICON,
        AddonFont.SHINCONIC to Fonts.SHINCONIC,
        AddonFont.MOJANGLES to Fonts.MOJANGLES,
        AddonFont.UNIFONT to Fonts.UNIFONT
    )

    private fun toColor(c: AddonColor) = Color(c.r, c.g, c.b, c.a)

    private fun getFont(f: AddonFont) = fonts[f] ?: Fonts.REGULAR

    override fun drawText(text: String, x: Float, y: Float, color: AddonColor, size: Float, font: AddonFont) {
        nvg?.drawText(text, x, y, toColor(color), size, getFont(font))
    }

    override fun drawCenteredText(text: String, x: Float, y: Float, color: AddonColor, size: Float, font: AddonFont) {
        nvg?.drawCenteredText(text, x, y, toColor(color), size, getFont(font))
    }

    override fun drawTextBox(text: String, x: Float, y: Float, maxWidth: Float, color: AddonColor, size: Float, font: AddonFont) {
        nvg?.drawTextBox(text, x, y, maxWidth, toColor(color), size, getFont(font))
    }

    override fun drawTextGlowing(text: String, x: Float, y: Float, color: AddonColor, blurRadius: Float, size: Float, font: AddonFont) {
        nvg?.drawTextGlowing(text, x, y, toColor(color), blurRadius, size, getFont(font))
    }

    override fun drawCenteredIcon(icon: String, x: Float, y: Float, size: Float, color: AddonColor) {
        nvg?.drawCenteredIcon(icon, x, y, size, toColor(color))
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float, color: AddonColor) {
        nvg?.drawRect(x, y, width, height, toColor(color))
    }

    override fun drawRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: AddonColor) {
        nvg?.drawRoundedRect(x, y, width, height, radius, toColor(color))
    }

    override fun drawVerticalGradientRect(x: Float, y: Float, width: Float, height: Float, color1: AddonColor, color2: AddonColor) {
        nvg?.drawVerticalGradientRect(x, y, width, height, toColor(color1), toColor(color2))
    }

    override fun drawHorizontalGradientRect(x: Float, y: Float, width: Float, height: Float, color1: AddonColor, color2: AddonColor) {
        nvg?.drawHorizontalGradientRect(x, y, width, height, toColor(color1), toColor(color2))
    }

    override fun drawGradientRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color1: AddonColor, color2: AddonColor) {
        nvg?.drawGradientRoundedRect(x, y, width, height, radius, toColor(color1), toColor(color2))
    }

    override fun drawOutlineRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, strokeWidth: Float, color: AddonColor) {
        nvg?.drawOutlineRoundedRect(x, y, width, height, radius, strokeWidth, toColor(color))
    }

    override fun drawShadow(x: Float, y: Float, width: Float, height: Float, radius: Float, strength: Int) {
        nvg?.drawShadow(x, y, width, height, radius, strength)
    }

    override fun drawCircle(x: Float, y: Float, radius: Float, color: AddonColor) {
        nvg?.drawCircle(x, y, radius, toColor(color))
    }

    override fun drawGradientCircle(x: Float, y: Float, radius: Float, color1: AddonColor, color2: AddonColor) {
        nvg?.drawGradientCircle(x, y, radius, toColor(color1), toColor(color2))
    }

    override fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, strokeWidth: Float, color: AddonColor) {
        nvg?.drawLine(x1, y1, x2, y2, strokeWidth, toColor(color))
    }

    override fun drawArc(cx: Float, cy: Float, r: Float, a0: Float, a1: Float, strokeWidth: Float, color: AddonColor) {
        nvg?.drawArc(cx, cy, r, a0, a1, strokeWidth, toColor(color))
    }

    override fun getTextWidth(text: String, fontSize: Float, font: AddonFont): Float {
        return nvg?.getTextWidth(text, fontSize, getFont(font)) ?: 0f
    }

    override fun getTextHeight(text: String, fontSize: Float, font: AddonFont): Float {
        return nvg?.getTextHeight(text, fontSize, getFont(font)) ?: 0f
    }

    override fun getLimitText(text: String, fontSize: Float, font: AddonFont, maxWidth: Float): String {
        return nvg?.getLimitText(text, fontSize, getFont(font), maxWidth) ?: text
    }

    override fun save() {
        nvg?.save()
    }

    override fun restore() {
        nvg?.restore()
    }

    override fun scissor(x: Float, y: Float, width: Float, height: Float) {
        nvg?.scissor(x, y, width, height)
    }

    override fun translate(x: Float, y: Float) {
        nvg?.translate(x, y)
    }

    override fun drawImage(path: String, x: Float, y: Float, width: Float, height: Float) {
        when {
            path.contains(":") -> nvg?.drawImage(ResourceLocation(path), x, y, width, height)
            else -> nvg?.drawImage(File(path), x, y, width, height)
        }
    }

    override fun drawImage(path: String, x: Float, y: Float, width: Float, height: Float, alpha: Int) {
        val a = alpha.coerceIn(0, 255) / 255f
        when {
            path.contains(":") -> nvg?.drawImage(ResourceLocation(path), x, y, width, height, alpha)
            else -> nvg?.drawRoundedImage(File(path), x, y, width, height, 0.01f, a)
        }
    }

    override fun drawRoundedImage(path: String, x: Float, y: Float, width: Float, height: Float, radius: Float) {
        when {
            path.contains(":") -> nvg?.drawRoundedImage(ResourceLocation(path), x, y, width, height, radius)
            else -> nvg?.drawRoundedImage(File(path), x, y, width, height, radius)
        }
    }

    override fun drawRoundedImage(path: String, x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Int) {
        val a = alpha.coerceIn(0, 255) / 255f
        when {
            path.contains(":") -> nvg?.drawRoundedImage(ResourceLocation(path), x, y, width, height, radius, a)
            else -> nvg?.drawRoundedImage(File(path), x, y, width, height, radius, a)
        }
    }

    override fun drawPlayerHead(playerName: String, x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Float) {
        val location = PlayerHeadUtils.getOrRequest(playerName) ?: return
        nvg?.drawPlayerHead(location, x, y, width, height, radius, alpha.coerceIn(0f, 1f))
    }
}
