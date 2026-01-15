package me.miki.shindo.ui.comp.base

/**
 * Interface base para todos os componentes da UI.
 * Define o contrato mínimo que um componente deve implementar.
 */
interface IComponent {
    /**
     * Renderiza o componente na tela.
     * @param mouseX Posição X do mouse
     * @param mouseY Posição Y do mouse
     * @param partialTicks Ticks parciais para animações suaves
     */
    fun draw(mouseX: Int, mouseY: Int, partialTicks: Float)

    /**
     * Atualiza o estado do componente (animações, lógica, etc).
     * @param partialTicks Ticks parciais para animações suaves
     */
    fun update(partialTicks: Float)

    /**
     * Manipula cliques do mouse.
     * @param mouseX Posição X do mouse
     * @param mouseY Posição Y do mouse
     * @param mouseButton Botão do mouse (0 = esquerdo, 1 = direito, 2 = meio)
     */
    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int)

    /**
     * Manipula soltura do botão do mouse.
     * @param mouseX Posição X do mouse
     * @param mouseY Posição Y do mouse
     * @param mouseButton Botão do mouse
     */
    fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int)

    /**
     * Manipula entrada de teclado.
     * @param typedChar Caractere digitado
     * @param keyCode Código da tecla
     */
    fun keyTyped(typedChar: Char, keyCode: Int)

    /**
     * Verifica se o componente está visível.
     */
    fun isVisible(): Boolean

    /**
     * Define a visibilidade do componente.
     */
    fun setVisible(visible: Boolean)
}
