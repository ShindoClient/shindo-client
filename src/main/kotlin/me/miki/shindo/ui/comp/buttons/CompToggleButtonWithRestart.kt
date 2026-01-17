package me.miki.shindo.ui.comp.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.ui.comp.display.CompTooltip
import me.miki.shindo.ui.comp.templates.CompInteractive
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.animation.ColorAnimation
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import java.awt.Color

/**
 * Toggle button que mostra um aviso quando a setting requer reinício do jogo.
 */
class CompToggleButtonWithRestart : CompInteractive {
    private val opacityAnimation = SimpleAnimation()
    private val toggleAnimation = SimpleAnimation()
    private val circleAnimation = ColorAnimation()
    private val warningAnimation = SimpleAnimation()

    private val setting: BooleanSetting
    private val requiresRestart: Boolean
    private var scale: Float = 1.0f
    private var showWarning: Boolean = false
    private var warningHovered: Boolean = false
    private val tooltip: CompTooltip by lazy {
        CompTooltip(TranslateText.PERFORMANCE_RESTART_REQUIRED.text, 0f, 0f)
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
        initState()
    }

    constructor(setting: BooleanSetting, requiresRestart: Boolean = false) : super(0f, 0f) {
        this.setting = setting
        this.requiresRestart = requiresRestart
        setScale(1.0f)
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
            ColorUtils.applyAlpha(accentColor.color1, (opacityAnimation.value * 255).toInt()),
            ColorUtils.applyAlpha(accentColor.color2, (opacityAnimation.value * 255).toInt())
        )
        nvgInstance.drawRoundedRect(
            x + toggleAnimation.value * scale,
            y + 2.5f * scale,
            circle,
            circle,
            circle / 2,
            circleAnimation.getColor(if (toggled) Color.WHITE else palette.getBackgroundColor(ColorType.DARK), 16)
        )
        
        // Desenha aviso de reinício se necessário
        if (requiresRestart && (showWarning || warningAnimation.value > 0.01f)) {
            val warningX = x + width + 8f
            val warningY = y + (height / 2f) - 8f
            val warningAlpha = (warningAnimation.value * 255).toInt()
            val warningColor = ColorUtils.applyAlpha(Color(255, 200, 0), warningAlpha)
            
            // Verifica se o mouse está sobre o aviso
            val warningHoveredNow = mouseX >= warningX && mouseX <= warningX + 16f &&
                                    mouseY >= warningY && mouseY <= warningY + 16f
            warningHovered = warningHoveredNow
            
            // Ícone de aviso (triângulo de alerta)
            nvgInstance.drawCenteredText(
                LegacyIcon.ALERT_TRIANGLE,
                warningX + 8f,
                warningY + 8f,
                warningColor,
                12f,
                Fonts.LEGACYICON
            )
            
            // Mostra tooltip se hovered
            if (warningHovered) {
                tooltip.setX(warningX + 20f)
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
            
            // Mostra aviso se requer reinício
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
        
        // Inclui área do aviso no hover
        val warningWidth = if (requiresRestart && warningAnimation.value > 0.01f) 20f else 0f
        
        return mouseX >= x && mouseX <= x + width + warningWidth &&
               mouseY >= y && mouseY <= y + height
    }
}
