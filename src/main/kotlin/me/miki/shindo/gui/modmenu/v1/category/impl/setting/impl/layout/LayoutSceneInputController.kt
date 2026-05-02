package me.miki.shindo.gui.modmenu.v1.category.impl.setting.impl.layout

import me.miki.shindo.utils.mouse.MouseUtils
import org.lwjgl.input.Keyboard

/**
 * Input predicates for [LayoutScene].
 */
class LayoutSceneInputController {

    fun isPrimaryClick(mouseButton: Int): Boolean {
        return mouseButton == 0
    }

    fun isBackMouseButton(mouseButton: Int): Boolean {
        return mouseButton == 3
    }

    fun shouldCloseByOutsideClick(
        mouseX: Int,
        mouseY: Int,
        baseX: Float,
        baseY: Float,
        baseWidth: Float,
        baseHeight: Float
    ): Boolean {
        return !MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)
    }

    fun shouldCloseByEscape(keyCode: Int): Boolean {
        return keyCode == Keyboard.KEY_ESCAPE
    }
}
