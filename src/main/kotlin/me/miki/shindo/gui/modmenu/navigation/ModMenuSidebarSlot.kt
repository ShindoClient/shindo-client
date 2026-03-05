package me.miki.shindo.gui.modmenu.navigation

import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.utils.mouse.MouseUtils

/**
 * Runtime sidebar slot metadata used by ModMenu shell rendering/input.
 */
data class ModMenuSidebarSlot(
    val category: Category,
    val x: Float,
    val y: Float,
    val size: Float
) {
    fun contains(mouseX: Int, mouseY: Int): Boolean {
        return MouseUtils.isInside(mouseX, mouseY, x, y, size, size)
    }
}

