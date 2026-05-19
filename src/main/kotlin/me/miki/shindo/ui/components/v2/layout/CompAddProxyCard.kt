package me.miki.shindo.ui.components.v2.layout

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.ui.components.v2.Component
import me.miki.shindo.utils.ColorUtils

class CompAddProxyCard : Component() {
    var label: String = "Add Proxy"
    var onClick: (() -> Unit)? = null

    private val hoverAnimation = SimpleAnimation()

    override fun draw(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        if (!isVisible()) return

        val hovered = isHovered(mouseX, mouseY)
        hoverAnimation.setAnimation(if (hovered) 1.0f else 0.0f, 14.0)
        val hoverProgress = hoverAnimation.getValue()

        val base =
            ColorUtils.applyAlpha(
                palette.getBackgroundColor(ColorType.DARK),
                (190f + hoverProgress * 36f).toInt().coerceIn(0, 255),
            )
        val borderColor =
            if (hovered) {
                ColorUtils.applyAlpha(accent.getInterpolateColor(), 164)
            } else {
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 106)
            }

        nvg.drawShadow(getX(), getY(), getWidth(), getHeight(), CARD_RADIUS, if (hovered) 8 else 6)
        nvg.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), CARD_RADIUS, base)
        nvg.drawOutlineRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            CARD_RADIUS,
            BORDER_WIDTH,
            borderColor,
        )

        val iconHeight = nvg.getTextHeight(Lucide.PLUS, ICON_SIZE, Fonts.LUCIDE)
        val labelHeight = nvg.getTextHeight(label, LABEL_SIZE, Fonts.MEDIUM)
        val spacing = 6f
        val contentCenterY = getY() + getHeight() / 2f
        val iconBaselineY = contentCenterY - (labelHeight + spacing) / 2f - iconHeight * 0.5f
        val labelBaselineY = contentCenterY + spacing * 0.5f

        nvg.drawCenteredText(
            Lucide.PLUS,
            getX() + getWidth() / 2f,
            iconBaselineY,
            palette.getFontColor(ColorType.DARK),
            ICON_SIZE,
            Fonts.LUCIDE,
        )
        nvg.drawCenteredText(
            label,
            getX() + getWidth() / 2f,
            labelBaselineY,
            palette.getFontColor(ColorType.DARK),
            LABEL_SIZE,
            Fonts.MEDIUM,
        )
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
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
