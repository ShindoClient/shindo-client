package me.miki.shindo.ui.comp.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.style.CompControlVariant
import me.miki.shindo.ui.comp.templates.CompButton
import me.miki.shindo.utils.ColorUtils
import java.awt.Color
class CompActionButton(
        text: String,
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 80f,
        height: Float = 20f
) : CompButton(x, y, width, height) {

    init {
        setRadius(6f)
        setFontSize(10f)
        setVariant(CompControlVariant.PRIMARY)
        setText(text)
        setTextColor(Color.WHITE)
    }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent

        val bgColor = if (hovered && isEnabled()) {
            ColorUtils.applyAlpha(accentColors.getColor1(), 200)
        } else {
            paletteColors.getBackgroundColor(ColorType.NORMAL)
        }

        nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), getRadius(), bgColor)

        getText()?.let {
            val textY = getY() + getHeight() / 2f - getFontSize() / 2f
            nvgInstance.drawCenteredText(
                    it,
                    getX() + getWidth() / 2f,
                    textY,
                    getTextColor() ?: Color.WHITE,
                    getFontSize(),
                    Fonts.REGULAR
            )
        }
    }
}
