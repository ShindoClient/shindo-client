package me.miki.shindo.ui.comp.layout

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils

class CompProxyCard : Comp() {

    var title: String = ""
    var subtitle: String = ""
    var active: Boolean = false
    var statusActiveText: String = "Online"
    var statusInactiveText: String = "Offline"

    var onCardClick: (() -> Unit)? = null
    var onToggleClick: (() -> Unit)? = null

    private val toggleAnimation = SimpleAnimation()

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return

        val hovered = isHovered(mouseX, mouseY)
        val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (hovered) 220 else 190)
        val overlayStart = ColorUtils.applyAlpha(accent.getColor1(), if (active) 140 else if (hovered) 70 else 35)
        val overlayEnd = ColorUtils.applyAlpha(accent.getColor2(), if (active) 140 else if (hovered) 70 else 35)

        nvg.drawShadow(getX(), getY(), getWidth(), getHeight(), CARD_RADIUS, 6)
        nvg.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), CARD_RADIUS, base)
        nvg.drawGradientRoundedRect(getX(), getY(), getWidth(), getHeight(), CARD_RADIUS, overlayStart, overlayEnd)

        if (active) {
            nvg.drawGradientOutlineRoundedRect(
                getX(),
                getY(),
                getWidth(),
                getHeight(),
                CARD_RADIUS,
                ACTIVE_OUTLINE_WIDTH,
                ColorUtils.applyAlpha(accent.getColor1(), 200),
                ColorUtils.applyAlpha(accent.getColor2(), 200)
            )
        }

        nvg.drawText(
            title,
            getX() + TEXT_LEFT_PADDING,
            getY() + TITLE_TOP_PADDING,
            if (active) accent.getColor1() else palette.getFontColor(ColorType.NORMAL),
            TITLE_FONT_SIZE,
            Fonts.SEMIBOLD
        )
        nvg.drawText(
            subtitle,
            getX() + TEXT_LEFT_PADDING,
            getY() + SUBTITLE_TOP_PADDING,
            palette.getFontColor(ColorType.DARK),
            SUBTITLE_FONT_SIZE,
            Fonts.REGULAR
        )

        val statusText = if (active) statusActiveText else statusInactiveText
        nvg.drawText(
            statusText,
            getX() + getWidth() - STATUS_RIGHT_PADDING,
            getY() + STATUS_TOP_PADDING,
            if (active) accent.getColor1() else palette.getFontColor(ColorType.DARK),
            STATUS_FONT_SIZE,
            Fonts.MEDIUM
        )

        val toggleX = getX() + getWidth() - TOGGLE_WIDTH - TOGGLE_RIGHT_PADDING
        val toggleY = getY() + (getHeight() - TOGGLE_HEIGHT) / 2f
        toggleAnimation.setAnimation(if (active) 1.0f else 0.0f, 16.0)
        val toggleProgress = toggleAnimation.value

        val toggleRadius = TOGGLE_HEIGHT / 2f
        val toggleBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 200)
        nvg.drawRoundedRect(toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, toggleRadius, toggleBase)
        if (toggleProgress > 0f) {
            nvg.drawGradientRoundedRect(
                    toggleX,
                    toggleY,
                    TOGGLE_WIDTH,
                    TOGGLE_HEIGHT,
                    toggleRadius,
                    ColorUtils.applyAlpha(accent.getColor1(), (toggleProgress * 255).toInt()),
                    ColorUtils.applyAlpha(accent.getColor2(), (toggleProgress * 255).toInt())
            )
        }
        val knobSize = TOGGLE_HEIGHT - 6f
        val knobX = toggleX + 3f + toggleProgress * (TOGGLE_WIDTH - knobSize - 6f)
        val knobY = toggleY + 3f
        nvg.drawRoundedRect(knobX, knobY, knobSize, knobSize, knobSize / 2f, java.awt.Color.WHITE)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible() || mouseButton != 0 || !isHovered(mouseX, mouseY)) return

        val toggleX = getX() + getWidth() - TOGGLE_WIDTH - TOGGLE_RIGHT_PADDING
        val toggleY = getY() + (getHeight() - TOGGLE_HEIGHT) / 2f
        if (MouseUtils.isInside(mouseX, mouseY, toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT)) {
            onToggleClick?.invoke()
            return
        }
        onCardClick?.invoke()
    }

    private companion object {
        private const val CARD_RADIUS = 12f
        private const val ACTIVE_OUTLINE_WIDTH = 2f
        private const val TEXT_LEFT_PADDING = 20f
        private const val TITLE_TOP_PADDING = 20f
        private const val SUBTITLE_TOP_PADDING = 38f
        private const val STATUS_RIGHT_PADDING = 100f
        private const val STATUS_TOP_PADDING = 20f
        private const val TOGGLE_WIDTH = 44f
        private const val TOGGLE_HEIGHT = 18f
        private const val TOGGLE_RIGHT_PADDING = 20f
        private const val TITLE_FONT_SIZE = 13f
        private const val SUBTITLE_FONT_SIZE = 10f
        private const val STATUS_FONT_SIZE = 11f
    }
}
