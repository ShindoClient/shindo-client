package me.miki.shindo.ui.components.v2.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.ui.animation.v1.value.SimpleAnimation
import me.miki.shindo.ui.components.v1.display.CompTooltip
import me.miki.shindo.ui.components.v1.style.CompControlVariant
import me.miki.shindo.ui.components.v1.style.CompStyleResolver
import me.miki.shindo.ui.components.v1.templates.CompControlTemplate
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color

class CompToggleButtonWithRestart : CompControlTemplate {
    private val hoverAnimation = SimpleAnimation()
    private val pressAnimation = SimpleAnimation()
    private val toggleAnimation = SimpleAnimation()
    private val warningAnimation = SimpleAnimation()

    private val setting: BooleanSetting
    private val requiresRestart: Boolean
    private var scale: Float = 1.0f
    private var showWarning: Boolean = false
    private val tooltip: CompTooltip by lazy {
        CompTooltip(TranslateText.PERFORMANCE_RESTART_REQUIRED.getText(), 0f, 0f)
    }

    fun getSetting(): BooleanSetting = setting
    fun getScale(): Float = scale

    fun setShowWarning(show: Boolean) {
        showWarning = show
        warningAnimation.setAnimation(if (show) 1.0f else 0.0f, 12.0)
    }

    constructor(
        x: Float,
        y: Float,
        scale: Float,
        setting: BooleanSetting,
        requiresRestart: Boolean = false
    ) : super(x, y) {
        this.setting = setting
        this.requiresRestart = requiresRestart
        setScale(scale)
        setVariant(CompControlVariant.SECONDARY)
        initState()
    }

    constructor(setting: BooleanSetting, requiresRestart: Boolean = false) : super(0f, 0f) {
        this.setting = setting
        this.requiresRestart = requiresRestart
        setScale(1.0f)
        setVariant(CompControlVariant.SECONDARY)
        initState()
    }

    private fun initState() {
        toggleAnimation.value = if (setting.isToggled()) 1.0f else 0.0f
        warningAnimation.value = 0.0f
    }

    fun setScale(scale: Float) {
        this.scale = scale
        super.setWidth(34f * scale)
        super.setHeight(16f * scale)
    }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val nvgInstance = nvg
        val accentColor = accent
        val palette = palette

        val x = getX()
        val y = getY()
        val width = getWidth()
        val height = getHeight()
        val enabled = isEnabled()
        val toggled = setting.isToggled()
        val radius = height / 2f
        val knobSize = (height - (4f * scale)).coerceAtLeast(8f * scale)
        val knobInset = (height - knobSize) / 2f
        val knobTravel = width - (knobInset * 2f) - knobSize

        hoverAnimation.setAnimation(if (hovered && enabled) 1.0f else 0.0f, 16.0)
        pressAnimation.setAnimation(if (pressAnimation.value > 0.08f) pressAnimation.value * 0.83f else 0.0f, 16.0)
        toggleAnimation.setAnimation(if (toggled) 1.0f else 0.0f, 16.0)
        val warningVisible = requiresRestart && (showWarning || warningAnimation.value > 0.01f)
        warningAnimation.setAnimation(if (warningVisible) 1.0f else 0.0f, 12.0)

        val baseTrack = CompStyleResolver.resolveControlBase(CompControlVariant.SECONDARY, palette, accentColor)
        val hoverTrack = CompStyleResolver.resolveControlHover(CompControlVariant.SECONDARY, palette, accentColor)
        var trackColor = ColorUtils.interpolateColor(baseTrack, hoverTrack, hoverAnimation.value.toDouble())
        if (pressAnimation.value > 0.08f) {
            trackColor = ColorUtils.darken(trackColor, pressAnimation.value * 0.16f)
        }
        if (!enabled) {
            trackColor = ColorUtils.applyAlpha(trackColor, 116)
        }

        val activeGradientStart = ColorUtils.applyAlpha(
            accentColor.getColor1(),
            if (enabled) (80 + (toggleAnimation.value * 145f).toInt()) else 86
        )
        val activeGradientEnd = ColorUtils.applyAlpha(
            accentColor.getColor2(),
            if (enabled) (80 + (toggleAnimation.value * 145f).toInt()) else 86
        )

        val outlineIdle = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 34)
        val outlineHover = ColorUtils.applyAlpha(accentColor.getColor1(), 98)
        var outlineColor = ColorUtils.interpolateColor(outlineIdle, outlineHover, hoverAnimation.value.toDouble())
        if (!enabled) {
            outlineColor = ColorUtils.applyAlpha(outlineColor, 92)
        }

        nvgInstance.drawRoundedRect(x, y, width, height, radius, trackColor)
        nvgInstance.drawGradientRoundedRect(x, y, width, height, radius, activeGradientStart, activeGradientEnd)
        nvgInstance.drawOutlineRoundedRect(x, y, width, height, radius, 1f, outlineColor)

        val knobX = x + knobInset + knobTravel * toggleAnimation.value
        var knobColor = ColorUtils.interpolateColor(
            palette.getBackgroundColor(ColorType.DARK),
            Color.WHITE,
            toggleAnimation.value.toDouble()
        )
        if (hoverAnimation.value > 0.0f) {
            knobColor = ColorUtils.interpolateColor(knobColor, Color.WHITE, (hoverAnimation.value * 0.2f).toDouble())
        }
        if (!enabled) {
            knobColor = ColorUtils.applyAlpha(knobColor, 144)
        }

        nvgInstance.drawRoundedRect(
            knobX,
            y + knobInset,
            knobSize,
            knobSize,
            knobSize / 2f,
            knobColor
        )
        nvgInstance.drawOutlineRoundedRect(
            knobX,
            y + knobInset,
            knobSize,
            knobSize,
            knobSize / 2f,
            1f,
            ColorUtils.applyAlpha(Color.BLACK, if (enabled) 42 else 22)
        )

        if (warningVisible) {
            val warningSize = 11f
            val warningX = x - 14f - nvg.getTextWidth(LegacyIcon.ALERT_TRIANGLE, warningSize, Fonts.LEGACYICON) / 2f
            val warningY =
                y + (height / 2f) - nvg.getTextHeight(LegacyIcon.ALERT_TRIANGLE, warningSize, Fonts.LEGACYICON) / 2f
            val warningAlpha = (warningAnimation.value * 255).toInt().coerceIn(0, 255)
            val warningColor = ColorUtils.applyAlpha(Color(255, 189, 64), warningAlpha)

            nvgInstance.drawText(
                LegacyIcon.ALERT_TRIANGLE,
                warningX,
                warningY,
                warningColor,
                warningSize,
                Fonts.LEGACYICON
            )

            if (MouseUtils.isInside(mouseX, mouseY, warningX - 3f, warningY - 3f, 16f, 16f)) {
                tooltip.setX(warningX - 6f - tooltip.getWidth())
                tooltip.setY(warningY - 2f)
                tooltip.show()
                tooltip.draw(mouseX, mouseY, partialTicks)
            } else {
                tooltip.hide()
            }
        } else {
            tooltip.hide()
        }
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && isEnabled()) {
            pressAnimation.value = 1.0f
            val newValue = !setting.isToggled()
            setting.setToggled(newValue)

            if (requiresRestart) {
                setShowWarning(true)
            }
        }
    }

    override fun isHoveredInteractive(mouseX: Int, mouseY: Int): Boolean {
        val warningWidth = if (requiresRestart && warningAnimation.value > 0.01f) 18f else 0f
        return mouseX >= getX() - warningWidth &&
                mouseX <= getX() + getWidth() &&
                mouseY >= getY() &&
                mouseY <= getY() + getHeight()
    }
}
