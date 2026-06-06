package com.shindoclient.shindo.gui.modmenu.v2.navigation

import com.shindoclient.shindo.gui.modmenu.v2.category.Category
import com.shindoclient.shindo.utils.mouse.MouseUtils

/**
 * Runtime sidebar slot metadata used by ModMenu shell rendering/input.
 */
data class ModMenuSidebarSlot(
    val category: Category,
    val x: Float,
    val y: Float,
    val size: Float,
) {
    fun contains(
        mouseX: Int,
        mouseY: Int,
    ): Boolean = MouseUtils.isInside(mouseX, mouseY, x, y, size, size)
}
