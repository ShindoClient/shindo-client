package me.miki.shindo.ui.comp.frame

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.comp.templates.CompLabel
import java.awt.Color

/**
 * Label otimizado para uso em frames.
 * Mantém o estilo do cliente e funciona tanto dentro quanto fora de frames.
 */
class CompFrameLabel(
    x: Float = 0f,
    y: Float = 0f,
    text: String = "",
    fontSize: Float = 10f
) : CompLabel(x, y, text) {
    
    init {
        setFontSize(fontSize)
    }
}
