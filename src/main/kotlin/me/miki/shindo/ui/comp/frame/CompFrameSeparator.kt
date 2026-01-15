package me.miki.shindo.ui.comp.frame

import me.miki.shindo.ui.comp.layout.CompSeparator

/**
 * Separador otimizado para uso em frames.
 * Mantém o estilo do cliente e funciona tanto dentro quanto fora de frames.
 * É um alias para CompSeparator com configurações padrão otimizadas.
 */
class CompFrameSeparator(
    x: Float = 0f,
    y: Float = 0f,
    length: Float = 200f,
    orientation: CompSeparator.Orientation = CompSeparator.Orientation.HORIZONTAL
) : CompSeparator(x, y, length, orientation) {
    // Usa a implementação padrão do CompSeparator
}
