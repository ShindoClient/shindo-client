package me.miki.shindo.ui.comp.display

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.comp.templates.CompDisplay
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

class CompProgressBar(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 100f,
    height: Float = 8f
) : CompDisplay(x, y) {

    private val progressAnimation = SimpleAnimation()
    private val textAnimation = SimpleAnimation()
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
        this.maxProgress = value.coerceAtLeast(0.0001f)
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

        val bgColor = backgroundColor ?: ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.NORMAL), 210)
        val defaultProgressStart = ColorUtils.applyAlpha(accentColors.getColor1(), 228)
        val defaultProgressEnd = ColorUtils.applyAlpha(accentColors.getColor2(), 228)
        val customProgress = progressColor

        val max = maxProgress.coerceAtLeast(0.0001f)
        val progressRatio = (progress / max).coerceIn(0f, 1f)
        val animatedProgress = if (animated) {
            progressAnimation.setAnimation(progressRatio, 16.0)
            progressAnimation.value
        } else {
            progressRatio
        }
        textAnimation.setAnimation(if (showText) 1.0f else 0.0f, 16.0)

        val progressWidth = getWidth() * animatedProgress

        nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), radius, bgColor)
        nvgInstance.drawOutlineRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            radius,
            1f,
            ColorUtils.applyAlpha(paletteColors.getFontColor(ColorType.NORMAL), 54)
        )

        if (progressWidth > 0f) {
            if (customProgress != null) {
                nvgInstance.drawRoundedRect(getX(), getY(), progressWidth, getHeight(), radius, customProgress)
            } else {
                nvgInstance.drawGradientRoundedRect(
                    getX(),
                    getY(),
                    progressWidth,
                    getHeight(),
                    radius,
                    defaultProgressStart,
                    defaultProgressEnd
                )
            }
        }

        if (textAnimation.value > 0.01f) {
            val text = "${(progressRatio * 100).toInt()}%"
            val textColor = this.textColor ?: paletteColors.getFontColor(
                ColorType.DARK,
                (textAnimation.value * 255).toInt().coerceIn(0, 255)
            )
            val textSize = 8f
            val textHeight =
                nvgInstance.getTextHeight(text, textSize, me.miki.shindo.management.nanovg.font.Fonts.REGULAR)
            nvgInstance.drawCenteredText(
                text,
                getX() + getWidth() / 2f,
                getY() + getHeight() / 2f - textHeight / 2f,
                textColor,
                textSize,
                me.miki.shindo.management.nanovg.font.Fonts.REGULAR
            )
        }
    }
}
