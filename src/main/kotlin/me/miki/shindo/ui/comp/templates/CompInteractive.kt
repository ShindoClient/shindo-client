package me.miki.shindo.ui.comp.templates

import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.ui.comp.base.IInteractive
import me.miki.shindo.utils.mouse.MouseUtils

/**
 * Template base para componentes interativos (clicáveis, hover, etc).
 * Fornece funcionalidades comuns de interação como hover detection, callbacks, etc.
 */
abstract class CompInteractive(
    x: Float = 0f,
    y: Float = 0f
) : Comp(x, y), IInteractive {

    private var enabled: Boolean = true
    private var hovered: Boolean = false
    private var lastMouseX: Int = -1
    private var lastMouseY: Int = -1

    override var onClick: (() -> Unit)? = null
    override var onHoverEnter: (() -> Unit)? = null
    override var onHoverExit: (() -> Unit)? = null

    override fun isEnabled(): Boolean = enabled
    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun isHoveredInteractive(mouseX: Int, mouseY: Int): Boolean {
        return enabled && MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return

        // Atualiza estado de hover
        val currentlyHovered = isHoveredInteractive(mouseX, mouseY)
        if (currentlyHovered != hovered) {
            hovered = currentlyHovered
            if (hovered) {
                onHoverEnter?.invoke()
            } else {
                onHoverExit?.invoke()
            }
        }

        lastMouseX = mouseX
        lastMouseY = mouseY

        drawInteractive(mouseX, mouseY, partialTicks, hovered)
        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible() || !enabled) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        if (mouseButton == 0 && isHoveredInteractive(mouseX, mouseY)) {
            onClick?.invoke()
            onMouseClicked(mouseX, mouseY, mouseButton)
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Método abstrato para renderização específica do componente interativo.
     * @param mouseX Posição X do mouse
     * @param mouseY Posição Y do mouse
     * @param partialTicks Ticks parciais
     * @param hovered Se o componente está sendo hovered
     */
    protected abstract fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean)

    /**
     * Método chamado quando o componente é clicado (pode ser sobrescrito).
     */
    protected open fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {}
}
