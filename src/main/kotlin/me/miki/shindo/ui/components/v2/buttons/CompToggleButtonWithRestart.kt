package me.miki.shindo.ui.components.v2.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.ui.components.v2.display.CompTooltip
import me.miki.shindo.ui.components.v2.templates.CompControlTemplate
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color

class CompToggleButtonWithRestart(
    private val setting: BooleanSetting,
    private val requiresRestart: Boolean = false
) : CompControlTemplate(0f, 0f) {

    private val hoverAnim = SimpleAnimation()
    private val pressAnim = SimpleAnimation()
    private val toggleAnim = SimpleAnimation()
    private val warningAnim = SimpleAnimation()

    var scale: Float = 1.0f
        set(value) {
            field = value
            setWidth(34f * value)
            setHeight(16f * value)
        }

    private var showWarning = false

    private val tooltip by lazy {
        CompTooltip(TranslateText.PERFORMANCE_RESTART_REQUIRED.getText(), 0f, 0f)
    }

    init {
        scale = 1.0f
        toggleAnim.setValue(if (setting.isToggled()) 1f else 0f)
        warningAnim.setValue(0f)
    }

    fun setShowWarning(show: Boolean) {
        showWarning = show
        warningAnim.setAnimation(if (show) 1f else 0f, 12.0)
    }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val x = getX(); val y = getY()
        val w = getWidth(); val h = getHeight()
        val enabled = isEnabled()
        val toggled = setting.isToggled()

        val radius = h / 2f
        val knobSize = (h - 4f * scale).coerceAtLeast(8f * scale)
        val knobInset = (h - knobSize) / 2f
        val knobTravel = w - knobInset * 2f - knobSize

        hoverAnim.setAnimation(if (hovered && enabled) 1f else 0f, 16.0)
        pressAnim.setAnimation(if (pressAnim.getValue() > 0.08f) pressAnim.getValue() * 0.83f else 0f, 16.0)
        toggleAnim.setAnimation(if (toggled) 1f else 0f, 16.0)

        val warningVisible = requiresRestart && (showWarning || warningAnim.getValue() > 0.01f)
        warningAnim.setAnimation(if (warningVisible) 1f else 0f, 12.0)

        drawTrack(x, y, w, h, radius, enabled)
        drawKnob(x, y, knobInset, knobSize, knobTravel, enabled)
        if (warningVisible) drawWarning(mouseX, mouseY, partialTicks, x, y, h)
        else tooltip.hide()
    }

    private fun drawTrack(x: Float, y: Float, w: Float, h: Float, radius: Float, enabled: Boolean) {
        var trackColor = ColorUtils.interpolateColor(
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210),
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 165),
            hoverAnim.getValue().toDouble()
        )
        if (pressAnim.getValue() > 0.08f) trackColor = ColorUtils.darken(trackColor, pressAnim.getValue() * 0.16f)
        if (!enabled) trackColor = ColorUtils.applyAlpha(trackColor, 116)

        val alphaBase = if (enabled) (80 + (toggleAnim.getValue() * 145f).toInt()) else 86
        val gradStart = ColorUtils.applyAlpha(accent.getColor1(), alphaBase)
        val gradEnd = ColorUtils.applyAlpha(accent.getColor2(), alphaBase)

        var outlineColor = ColorUtils.interpolateColor(
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 34),
            ColorUtils.applyAlpha(accent.getColor1(), 98),
            hoverAnim.getValue().toDouble()
        )
        if (!enabled) outlineColor = ColorUtils.applyAlpha(outlineColor, 92)

        nvg.drawRoundedRect(x, y, w, h, radius, trackColor)
        nvg.drawGradientRoundedRect(x, y, w, h, radius, gradStart, gradEnd)
        nvg.drawOutlineRoundedRect(x, y, w, h, radius, 1f, outlineColor)
    }

    private fun drawKnob(x: Float, y: Float, inset: Float, size: Float, travel: Float, enabled: Boolean) {
        val knobX = x + inset + travel * toggleAnim.getValue()
        var color = ColorUtils.interpolateColor(
            palette.getBackgroundColor(ColorType.DARK),
            Color.WHITE,
            toggleAnim.getValue().toDouble()
        )
        if (hoverAnim.getValue() > 0f)
            color = ColorUtils.interpolateColor(color, Color.WHITE, (hoverAnim.getValue() * 0.2f).toDouble())
        if (!enabled) color = ColorUtils.applyAlpha(color, 144)

        nvg.drawRoundedRect(knobX, y + inset, size, size, size / 2f, color)
        nvg.drawOutlineRoundedRect(
            knobX, y + inset, size, size, size / 2f, 1f,
            ColorUtils.applyAlpha(Color.BLACK, if (enabled) 42 else 22)
        )
    }

    private fun drawWarning(mouseX: Int, mouseY: Int, partialTicks: Float, x: Float, y: Float, h: Float) {
        val wSize = 11f
        val wX = x - 14f - nvg.getTextWidth(LegacyIcon.ALERT_TRIANGLE, wSize, Fonts.LEGACYICON) / 2f
        val wY = y + h / 2f - nvg.getTextHeight(LegacyIcon.ALERT_TRIANGLE, wSize, Fonts.LEGACYICON) / 2f
        val wColor = ColorUtils.applyAlpha(Color(255, 189, 64), (warningAnim.getValue() * 255).toInt().coerceIn(0, 255))

        nvg.drawText(LegacyIcon.ALERT_TRIANGLE, wX, wY, wColor, wSize, Fonts.LEGACYICON)

        if (MouseUtils.isInside(mouseX, mouseY, wX - 3f, wY - 3f, 16f, 16f)) {
            tooltip.setX(wX - 6f - tooltip.getWidth())
            tooltip.setY(wY - 2f)
            tooltip.show()
            tooltip.draw(mouseX, mouseY, partialTicks)
        } else {
            tooltip.hide()
        }
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && isEnabled()) {
            pressAnim.setValue(1f)
            setting.setToggled(!setting.isToggled())
            if (requiresRestart) setShowWarning(true)
        }
    }

    override fun isHoveredInteractive(mouseX: Int, mouseY: Int): Boolean {
        val warningOffset = if (requiresRestart && warningAnim.getValue() > 0.01f) 18f else 0f
        return mouseX >= getX() - warningOffset &&
                mouseX <= getX() + getWidth() &&
                mouseY >= getY() &&
                mouseY <= getY() + getHeight()
    }

    fun getSetting() = setting
}