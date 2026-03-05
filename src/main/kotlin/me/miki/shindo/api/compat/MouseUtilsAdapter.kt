package me.miki.shindo.api.compat

import me.miki.client_api.util.IMouseUtils
import me.miki.shindo.utils.mouse.MouseUtils

class MouseUtilsAdapter : IMouseUtils {

    override fun isInside(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, height: Float): Boolean {
        return MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
    }
}
