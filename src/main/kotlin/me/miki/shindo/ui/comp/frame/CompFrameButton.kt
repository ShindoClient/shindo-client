package me.miki.shindo.ui.comp.frame

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.comp.templates.CompButton
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

/**
 * Botão otimizado para uso em frames.
 * Mantém o estilo do cliente e funciona tanto dentro quanto fora de frames.
 */
class CompFrameButton(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 120f,
    height: Float = 28f,
    text: String = "",
    private val accentColor: AccentColor? = null
) : CompButton(x, y, width, height) {
    
    init {
        setText(text)
        setRadius(6f)
    }
    
    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val nvgInstance = nvg
        val paletteColors = palette
        val accent = accentColor ?: this.accent
        val hoverProgress = if (hovered) 1.0f else 0.0f
        
        val radius = getRadius()
        val buttonX = getX()
        val buttonY = getY()
        val buttonWidth = getWidth()
        val buttonHeight = getHeight()
        
        // Glow effect quando hovered
        if (hoverProgress > 0.01f) {
            val glowStart = ColorUtils.applyAlpha(accent.color1, (80 + 140 * hoverProgress).toInt())
            val glowEnd = ColorUtils.applyAlpha(accent.color2, (80 + 140 * hoverProgress).toInt())
            nvgInstance.drawGradientShadow(buttonX, buttonY, buttonWidth, buttonHeight, radius, glowStart, glowEnd)
        }
        
        // Background
        val baseColor = paletteColors.getBackgroundColor(ColorType.NORMAL)
        val fillColor = ColorUtils.applyAlpha(baseColor, (200 + 40 * hoverProgress).toInt())
        nvgInstance.drawRoundedRect(buttonX, buttonY, buttonWidth, buttonHeight, radius, fillColor)
        
        // Outline quando hovered
        if (hoverProgress > 0.01f) {
            val outline = ColorUtils.applyAlpha(accent.color2, (80 + 90 * hoverProgress).toInt())
            nvgInstance.drawOutlineRoundedRect(buttonX, buttonY, buttonWidth, buttonHeight, radius, 1.0f, outline)
        }
        
        // Text - chama o método do pai para desenhar o texto
        super.drawInteractive(mouseX, mouseY, partialTicks, hovered)
    }
}
