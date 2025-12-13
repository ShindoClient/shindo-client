package me.miki.shindo.utils.mouse

object MouseUtils {

    @JvmStatic
    fun isInside(mouseX: Int, mouseY: Int, x: Double, y: Double, w: Double, h: Double): Boolean {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h
    }
}
