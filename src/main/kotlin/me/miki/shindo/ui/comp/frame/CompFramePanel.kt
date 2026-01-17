package me.miki.shindo.ui.comp.frame

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.comp.templates.CompPanel
import me.miki.shindo.utils.ColorUtils

/**
 * Panel otimizado para uso em frames.
 * Mantém o estilo do cliente e funciona tanto dentro quanto fora de frames.
 */
class CompFramePanel(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 200f,
    height: Float = 100f,
    radius: Float = 8f
) : CompPanel(x, y, width, height) {
    
    init {
        setRadius(radius)
    }
    
    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return
        
        val nvgInstance = nvg
        val paletteColors = palette
        
        // Background do panel
        val bgColor = ColorUtils.applyAlpha(
            paletteColors.getBackgroundColor(ColorType.MID),
            240
        )
        nvgInstance.drawRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            getRadius(),
            bgColor
        )
        
        // Renderiza componentes filhos
        super.draw(mouseX, mouseY, partialTicks)
    }
}
