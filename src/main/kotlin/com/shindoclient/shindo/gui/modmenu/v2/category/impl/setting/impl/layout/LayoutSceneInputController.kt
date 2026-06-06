package com.shindoclient.shindo.gui.modmenu.v2.category.impl.setting.impl.layout

import com.shindoclient.shindo.utils.mouse.MouseUtils
import org.lwjgl.input.Keyboard

class LayoutSceneInputController {
    fun isPrimaryClick(mouseButton: Int): Boolean = mouseButton == 0

    fun isBackMouseButton(mouseButton: Int): Boolean = mouseButton == 3

    fun shouldCloseByOutsideClick(
        mouseX: Int,
        mouseY: Int,
        baseX: Float,
        baseY: Float,
        baseWidth: Float,
        baseHeight: Float,
    ): Boolean = !MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)

    fun shouldCloseByEscape(keyCode: Int): Boolean = keyCode == Keyboard.KEY_ESCAPE
}
