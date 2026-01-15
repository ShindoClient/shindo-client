package me.miki.shindo.ui.comp.templates

import me.miki.shindo.ui.comp.Comp

/**
 * Template para componentes que apenas exibem informações (não interativos).
 * Otimizado para renderização de texto, imagens e outros elementos visuais.
 */
abstract class CompDisplay(
    x: Float = 0f,
    y: Float = 0f
) : Comp(x, y) {

    /**
     * Método abstrato para renderização específica do componente de exibição.
     * @param mouseX Posição X do mouse
     * @param mouseY Posição Y do mouse
     * @param partialTicks Ticks parciais
     */
    protected abstract fun drawDisplay(mouseX: Int, mouseY: Int, partialTicks: Float)

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return
        drawDisplay(mouseX, mouseY, partialTicks)
        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // Componentes de exibição não processam cliques
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
