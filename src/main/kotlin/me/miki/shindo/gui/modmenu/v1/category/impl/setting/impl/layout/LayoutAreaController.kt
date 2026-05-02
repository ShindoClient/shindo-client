package me.miki.shindo.gui.modmenu.v1.category.impl.setting.impl.layout

import me.miki.shindo.ui.components.v1.buttons.CompSceneButton
import me.miki.shindo.utils.mouse.MouseUtils

/**
 * Small controller wrapper used by [me.miki.shindo.gui.modmenu.v1.category.impl.setting.impl.LayoutScene].
 *
 * It keeps card rendering and hit-testing isolated from scene navigation logic.
 */
class LayoutAreaController(val scene: LayoutAreaScene) {
    private val button = CompSceneButton({ scene.icon }, { scene.name }, { scene.description })

    /**
     * Draws the scene entry card in the layout index list.
     */
    fun drawCard(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        active: Boolean,
        enabled: Boolean
    ) {
        button.setBounds(x, y, width, height)
        button.setActive(active)
        button.setEnabled(enabled)
        button.draw(mouseX, mouseY, partialTicks)
    }

    /**
     * Returns true when the pointer intersects this controller card.
     */
    fun hit(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, height: Float): Boolean {
        return MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
    }
}
