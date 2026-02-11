package me.miki.shindo.ui.comp.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.ui.comp.display.CompTooltip
import me.miki.shindo.ui.comp.style.CompControlVariant
import me.miki.shindo.ui.comp.templates.CompControlTemplate
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.ui.animation.value.ColorAnimation
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color
class CompToggleButtonWithRestart : CompControlTemplate {
    private val opacityAnimation = SimpleAnimation()
    private val toggleAnimation = SimpleAnimation()
    private val circleAnimation = ColorAnimation()
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

    constructor(x: Float, y: Float, scale: Float, setting: BooleanSetting, requiresRestart: Boolean = false) : super(x, y) {
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
        toggleAnimation.value = if (setting.isToggled()) 20.5f else 2.5f
        circleAnimation.setColor(
                if (setting.isToggled()) Color.WHITE else palette.getBackgroundColor(ColorType.DARK)
        )
        warningAnimation.value = 0.0f
    }

    fun setScale(scale: Float) {
        this.scale = scale
        super.setWidth(34F * scale)
        super.setHeight(16F * scale)
    }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val nvgInstance = nvg
        val accentColor = accent
        val palette = palette

        val x = getX()
        val y = getY()
        val width = getWidth()
        val height = getHeight()
        val circle = 11 * scale
        val toggled = setting.isToggled()

        opacityAnimation.setAnimation(if (toggled) 1.0f else 0.0f, 14.0)
        toggleAnimation.setAnimation(if (toggled) 20.5f else 2.5f, 14.0)

        nvgInstance.drawRoundedRect(x, y, width, height, 7 * scale, palette.getBackgroundColor(ColorType.NORMAL))

        nvgInstance.drawGradientRoundedRect(
                x,
                y,
                width,
                height,
                7.4f * scale,
                ColorUtils.applyAlpha(accentColor.getColor1(), (opacityAnimation.value * 255).toInt()),
                ColorUtils.applyAlpha(accentColor.getColor2(), (opacityAnimation.value * 255).toInt())
        )

        nvgInstance.drawRoundedRect(
                x + toggleAnimation.value * scale,
                y + 2.5f * scale,
                circle,
                circle,
                circle / 2,
                circleAnimation.getColor(if (toggled) Color.WHITE else palette.getBackgroundColor(ColorType.DARK), 16)
        )

        if (requiresRestart && (showWarning || warningAnimation.value > 0.01f)) {
            val warningX = x - 16f - nvg.getTextWidth(LegacyIcon.ALERT_TRIANGLE, 12f, Fonts.LEGACYICON) / 2f
            val warningY = y + (height / 2f) - nvg.getTextHeight(LegacyIcon.ALERT_TRIANGLE, 12f, Fonts.LEGACYICON) / 2f
            val warningAlpha = (warningAnimation.value * 255).toInt()
            val warningColor = ColorUtils.applyAlpha(Color(255, 200, 0), warningAlpha)

            nvgInstance.drawText(
                    LegacyIcon.ALERT_TRIANGLE,
                    warningX,
                    warningY,
                    warningColor,
                    12f,
                    Fonts.LEGACYICON
            )

            if (MouseUtils.isInside(mouseX, mouseY, warningX - 2f, warningY - 2f, 16f, 16f)) {
                tooltip.setX(warningX - 6F - tooltip.getWidth())
                tooltip.setY(warningY - 2f)
                tooltip.show()
                tooltip.draw(mouseX, mouseY, partialTicks)
            } else {
                tooltip.hide()
            }
        }
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            val newValue = !setting.isToggled()
            setting.setToggled(newValue)

            if (requiresRestart) {
                setShowWarning(true)
            }
        }
    }

    override fun isHoveredInteractive(mouseX: Int, mouseY: Int): Boolean {
        val x = getX()
        val y = getY()
        val width = getWidth()
        val height = getHeight()

        val warningWidth = if (requiresRestart && warningAnimation.value > 0.01f) 20f else 0f

        return mouseX >= x && mouseX <= x + width + warningWidth && mouseY >= y && mouseY <= y + height
    }
}
