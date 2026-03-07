package me.miki.shindo.gui.widget

import eu.shoroa.contrib.animation.Animate
import eu.shoroa.contrib.animation.Easing
import me.miki.shindo.management.nanovg.NanoVGManager
import java.util.function.Supplier

abstract class WidgetButtonBase(x: Float, y: Float, width: Float, height: Float, private val onClick: Runnable) : Widget(x, y, width, height) {
    private var wasClicked = false
    protected var isHovered = false

    /**
     * 0 = left click,
     * 1 = right click,
     * 2 = middle click
     */
    var actionButton = 0
    val hoverAnimation: Animate = Animate(5f, Easing.EXPO_OUT).easeIf(Supplier { isHovered })
    val clickAnimation: Animate = Animate(8f, Easing.QUINT_OUT).easeIf(Supplier { wasClicked })


    override fun render(renderer: NanoVGManager, mouseX: Float, mouseY: Float) {
        isHovered = isHovered(mouseX, mouseY)
        hoverAnimation.update()
        clickAnimation.update()
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (isHovered) {
            if (button == actionButton) {
                if (!wasClicked) {
                    wasClicked = true
                    return true
                }
            }
        }
        return false
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (isHovered) {
            if (wasClicked) {
                if (button == actionButton) {
                    onClick.run()
                    wasClicked = false
                    return true
                }
            }
        } else {
            wasClicked = false
        }
        return false
    }
}