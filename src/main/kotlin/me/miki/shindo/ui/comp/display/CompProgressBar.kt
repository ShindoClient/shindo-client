package me.miki.shindo.ui.comp.display

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.comp.templates.CompDisplay
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import java.awt.Color

/**
 * Barra de progresso animada.
 * Útil para mostrar progresso de operações, carregamento, etc.
 */
class CompProgressBar(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 100f,
    height: Float = 8f
) : CompDisplay(x, y) {

    private val progressAnimation = SimpleAnimation()
    private var progress: Float = 0f
    private var maxProgress: Float = 100f
    private var showText: Boolean = false
    private var textColor: Color? = null
    private var backgroundColor: Color? = null
    private var progressColor: Color? = null
    private var radius: Float = 4f
    private var animated: Boolean = true

    init {
        setWidth(width)
        setHeight(height)
    }

    fun getProgress(): Float = progress
    fun setProgress(value: Float) {
        this.progress = value.coerceIn(0f, maxProgress)
    }

    fun getMaxProgress(): Float = maxProgress
    fun setMaxProgress(value: Float) {
        this.maxProgress = value
        progress = progress.coerceIn(0f, maxProgress)
    }

    fun setShowText(show: Boolean): CompProgressBar {
        this.showText = show
        return this
    }

    fun setTextColor(color: Color?): CompProgressBar {
        this.textColor = color
        return this
    }

    fun setBackgroundColor(color: Color?): CompProgressBar {
        this.backgroundColor = color
        return this
    }

    fun setProgressColor(color: Color?): CompProgressBar {
        this.progressColor = color
        return this
    }

    fun setRadius(radius: Float): CompProgressBar {
        this.radius = radius
        return this
    }

    fun setAnimated(animated: Boolean): CompProgressBar {
        this.animated = animated
        return this
    }

    override fun drawDisplay(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent

        val bgColor = backgroundColor ?: paletteColors.getBackgroundColor(ColorType.NORMAL)
        val progColor = progressColor ?: accentColors.color1

        val progressRatio = (progress / maxProgress).coerceIn(0f, 1f)
        val animatedProgress = if (animated) {
            progressAnimation.setAnimation(progressRatio, 16.0)
            progressAnimation.value
        } else {
            progressRatio
        }

        val progressWidth = getWidth() * animatedProgress

        // Fundo
        nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), radius, bgColor)

        // Progresso
        if (progressWidth > 0f) {
            nvgInstance.drawRoundedRect(getX(), getY(), progressWidth, getHeight(), radius, progColor)
        }

        // Texto
        if (showText) {
            val text = "${(progressRatio * 100).toInt()}%"
            val textColor = this.textColor ?: paletteColors.getFontColor(ColorType.NORMAL)
            nvgInstance.drawCenteredText(
                text,
                getX() + getWidth() / 2f,
                getY() + getHeight() / 2f,
                textColor,
                8f,
                me.miki.shindo.management.nanovg.font.Fonts.REGULAR
            )
        }
    }
}
