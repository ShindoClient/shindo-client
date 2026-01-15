package me.miki.shindo.ui.comp.base

/**
 * Interface para componentes que podem ser interagidos (clicados, hover, etc).
 */
interface IInteractive {
    /**
     * Verifica se o componente está sendo hovered (mouse sobre ele).
     * @param mouseX Posição X do mouse
     * @param mouseY Posição Y do mouse
     */
    fun isHovered(mouseX: Int, mouseY: Int): Boolean

    /**
     * Verifica se o componente está habilitado para interação.
     */
    fun isEnabled(): Boolean

    /**
     * Define se o componente está habilitado.
     */
    fun setEnabled(enabled: Boolean)

    /**
     * Callback chamado quando o componente é clicado.
     */
    var onClick: (() -> Unit)?

    /**
     * Callback chamado quando o mouse entra no componente.
     */
    var onHoverEnter: (() -> Unit)?

    /**
     * Callback chamado quando o mouse sai do componente.
     */
    var onHoverExit: (() -> Unit)?
}
