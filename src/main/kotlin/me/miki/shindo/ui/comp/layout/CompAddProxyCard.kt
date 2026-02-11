package me.miki.shindo.ui.comp.layout

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.ColorUtils

class CompAddProxyCard : Comp() {

    var label: String = "Add Proxy"
    var onClick: (() -> Unit)? = null

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return

        val hovered = isHovered(mouseX, mouseY)
        val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (hovered) 220 else 190)
        val overlayStart = ColorUtils.applyAlpha(accent.getColor1(), if (hovered) 70 else 35)
        val overlayEnd = ColorUtils.applyAlpha(accent.getColor2(), if (hovered) 70 else 35)

        nvg.drawShadow(getX(), getY(), getWidth(), getHeight(), CARD_RADIUS, 6)
        nvg.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), CARD_RADIUS, base)
        nvg.drawGradientRoundedRect(getX(), getY(), getWidth(), getHeight(), CARD_RADIUS, overlayStart, overlayEnd)
        nvg.drawOutlineRoundedRect(getX(), getY(), getWidth(), getHeight(), CARD_RADIUS, BORDER_WIDTH, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 100))

        nvg.drawCenteredText(
            LegacyIcon.PLUS,
            getX() + getWidth() / 2f,
            getY() + getHeight() / 2f - 8f,
            palette.getFontColor(ColorType.DARK),
            ICON_SIZE,
            Fonts.LEGACYICON
        )
        nvg.drawCenteredText(
            label,
            getX() + getWidth() / 2f,
            getY() + getHeight() / 2f + 12f,
            palette.getFontColor(ColorType.DARK),
            LABEL_SIZE,
            Fonts.MEDIUM
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible() || mouseButton != 0 || !isHovered(mouseX, mouseY)) return
        onClick?.invoke()
    }

    private companion object {
        private const val CARD_RADIUS = 12f
        private const val BORDER_WIDTH = 1.2f
        private const val ICON_SIZE = 24f
        private const val LABEL_SIZE = 10f
    }
}
