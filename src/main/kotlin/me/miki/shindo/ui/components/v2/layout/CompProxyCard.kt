package me.miki.shindo.ui.components.v2.layout

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.ui.components.v2.Component
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color
import kotlin.math.max

class CompProxyCard : Component() {

    var title: String = ""
    var subtitle: String = ""
    var active: Boolean = false
    var statusActiveText: String = "Online"
    var statusInactiveText: String = "Offline"

    var onCardClick: (() -> Unit)? = null
    var onToggleClick: (() -> Unit)? = null

    private val toggleAnimation = SimpleAnimation()
    private val toggleHoverAnimation = SimpleAnimation()
    private val togglePressAnimation = SimpleAnimation()

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return

        val hovered = isHovered(mouseX, mouseY)
        val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (hovered) 216 else 194)
        val borderColor = when {
            active -> ColorUtils.applyAlpha(accent.getInterpolateColor(), 188)
            hovered -> ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 126)
            else -> ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 206)
        }

        nvg.drawShadow(getX(), getY(), getWidth(), getHeight(), CARD_RADIUS, 6)
        nvg.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), CARD_RADIUS, base)
        nvg.drawOutlineRoundedRect(getX(), getY(), getWidth(), getHeight(), CARD_RADIUS, 1f, borderColor)
        if (active) {
            nvg.drawRoundedRect(
                getX() + 10f,
                getY() + 13f,
                3f,
                getHeight() - 26f,
                1.5f,
                ColorUtils.applyAlpha(accent.getInterpolateColor(), 188)
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

        val toggleX = getX() + getWidth() - TOGGLE_WIDTH - TOGGLE_RIGHT_PADDING
        val toggleY = getY() + (getHeight() - TOGGLE_HEIGHT) / 2f
        val toggleHovered = MouseUtils.isInside(mouseX, mouseY, toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT)

        toggleHoverAnimation.setAnimation(if (toggleHovered) 1.0f else 0.0f, 14.0)
        togglePressAnimation.setAnimation(
            if (togglePressAnimation.getValue() > 0.08f) togglePressAnimation.getValue() * 0.82f else 0.0f,
            16.0
        )
        toggleAnimation.setAnimation(if (active) 1.0f else 0.0f, 16.0)

        val toggleProgress = toggleAnimation.getValue()
        val hoverProgress = toggleHoverAnimation.getValue()
        val pressProgress = togglePressAnimation.getValue()

        val statusText = if (active) statusActiveText else statusInactiveText
        val statusTextWidth = nvg.getTextWidth(statusText, STATUS_FONT_SIZE, Fonts.MEDIUM)
        val statusTextHeight = nvg.getTextHeight(statusText, STATUS_FONT_SIZE, Fonts.MEDIUM)
        val statusMinX = getX() + getWidth() * STATUS_MIN_X_RATIO
        val statusX = max(statusMinX, toggleX - STATUS_TOGGLE_GAP - statusTextWidth)
        val statusY = getY() + (getHeight() - statusTextHeight) / 2f - 0.6f

        nvg.drawText(
            statusText,
            statusX,
            statusY,
            if (active) accent.getColor1() else palette.getFontColor(ColorType.DARK),
            STATUS_FONT_SIZE,
            Fonts.MEDIUM
        )

        val toggleRadius = TOGGLE_HEIGHT / 2f
        var toggleBase = ColorUtils.applyAlpha(
            palette.getBackgroundColor(ColorType.NORMAL),
            if (toggleHovered) 224 else 200
        )
        if (pressProgress > 0.08f) {
            toggleBase = ColorUtils.darken(toggleBase, pressProgress * 0.15f)
        }

        nvg.drawRoundedRect(toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, toggleRadius, toggleBase)

        if (toggleProgress > 0f) {
            val activeAlpha = (toggleProgress * 190f + hoverProgress * 34f + 30f).toInt().coerceIn(0, 255)
            nvg.drawGradientRoundedRect(
                toggleX,
                toggleY,
                TOGGLE_WIDTH,
                TOGGLE_HEIGHT,
                toggleRadius,
                ColorUtils.applyAlpha(accent.getColor1(), activeAlpha),
                ColorUtils.applyAlpha(accent.getColor2(), activeAlpha)
            )
        }

        val toggleOutlineIdle = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 46)
        val toggleOutlineHover = ColorUtils.applyAlpha(accent.getColor1(), 130)
        val toggleOutline = ColorUtils.interpolateColor(toggleOutlineIdle, toggleOutlineHover, hoverProgress.toDouble())
        nvg.drawOutlineRoundedRect(toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, toggleRadius, 1f, toggleOutline)

        val knobSize = TOGGLE_HEIGHT - 6f
        val knobX = toggleX + 3f + toggleProgress * (TOGGLE_WIDTH - knobSize - 6f)
        val knobY = toggleY + 3f

        var knobColor = ColorUtils.interpolateColor(
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 236),
            Color.WHITE,
            toggleProgress.toDouble()
        )
        if (hoverProgress > 0.0f) {
            knobColor = ColorUtils.interpolateColor(knobColor, Color.WHITE, (hoverProgress * 0.18f).toDouble())
        }
        if (pressProgress > 0.08f) {
            knobColor = ColorUtils.darken(knobColor, pressProgress * 0.1f)
        }

        nvg.drawShadow(knobX, knobY, knobSize, knobSize, knobSize / 2f, 4)
        nvg.drawRoundedRect(knobX, knobY, knobSize, knobSize, knobSize / 2f, knobColor)
        nvg.drawOutlineRoundedRect(
            knobX,
            knobY,
            knobSize,
            knobSize,
            knobSize / 2f,
            1f,
            ColorUtils.applyAlpha(Color.BLACK, 36)
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible() || mouseButton != 0 || !isHovered(mouseX, mouseY)) return

        val toggleX = getX() + getWidth() - TOGGLE_WIDTH - TOGGLE_RIGHT_PADDING
        val toggleY = getY() + (getHeight() - TOGGLE_HEIGHT) / 2f
        if (MouseUtils.isInside(mouseX, mouseY, toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT)) {
            togglePressAnimation.setValue(1.0f)
            onToggleClick?.invoke()
            return
        }
        onCardClick?.invoke()
    }

    private companion object {
        private const val CARD_RADIUS = 12f
        private const val TEXT_LEFT_PADDING = 20f
        private const val TITLE_TOP_PADDING = 20f
        private const val SUBTITLE_TOP_PADDING = 38f
        private const val STATUS_MIN_X_RATIO = 0.48f
        private const val STATUS_TOGGLE_GAP = 10f
        private const val TOGGLE_WIDTH = 44f
        private const val TOGGLE_HEIGHT = 18f
        private const val TOGGLE_RIGHT_PADDING = 20f
        private const val TITLE_FONT_SIZE = 13f
        private const val SUBTITLE_FONT_SIZE = 10f
        private const val STATUS_FONT_SIZE = 11f
    }
}
