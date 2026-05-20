package me.miki.shindo.gui.modmenu.v2.category.impl.setting.impl.layout

import me.miki.shindo.ui.components.v2.buttons.CompSceneButton
import me.miki.shindo.utils.mouse.MouseUtils

class LayoutAreaController(
    val scene: LayoutAreaScene,
) {
    private val button = CompSceneButton({ scene.icon }, { scene.name }, { scene.description })

    fun drawCard(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        active: Boolean,
        enabled: Boolean,
    ) {
        button.setBounds(x, y, width, height)
        button.setActive(active)
        button.setEnabled(enabled)
        button.draw(mouseX, mouseY, partialTicks)
    }

    fun hit(
        mouseX: Int,
        mouseY: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ): Boolean = MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
}
