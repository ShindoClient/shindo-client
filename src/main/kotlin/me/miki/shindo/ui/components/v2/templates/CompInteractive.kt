package me.miki.shindo.ui.components.v2.templates

import me.miki.shindo.ui.components.v2.Component
import me.miki.shindo.ui.components.v2.base.IInteractive
import me.miki.shindo.utils.mouse.MouseUtils

abstract class CompInteractive(
    x: Float = 0f,
    y: Float = 0f
) : Component(x, y), IInteractive {

    private var enabled:     Boolean  =  true
    private var hovered:     Boolean  =  false
    private var lastMouseX:  Int      =  -1
    private var lastMouseY:  Int      =  -1

    override var onClick:       (() -> Unit)?  =  null
    override var onHoverEnter:  (() -> Unit)?  =  null
    override var onHoverExit:   (() -> Unit)?  =  null

    override fun isEnabled(): Boolean = enabled

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    open fun isHoveredInteractive(mouseX: Int, mouseY: Int): Boolean {
        return enabled && MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return

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

    protected abstract fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean)
    protected open fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {}
}
