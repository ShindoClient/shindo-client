package me.miki.shindo.utils.mouse

object MouseUtils {

    @JvmStatic
    fun isInside(mouseX: Int, mouseY: Int, x: Double, y: Double, w: Double, h: Double): Boolean {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h
    }

    @JvmStatic
    fun isInside(mouseX: Int, mouseY: Int, x: Float, y: Float, w: Float, h: Float): Boolean {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h
    }

    @JvmStatic
    fun isInside(mouseX: Int, mouseY: Int, x: Int, y: Int, w: Int, h: Int): Boolean {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h
    }

    @JvmStatic
    fun isInside(mouseX: Int, mouseY: Int, x: Long, y: Long, w: Long, h: Long): Boolean {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h
    }

    @JvmStatic
    fun isInside(mouseX: Int, mouseY: Int, x: Short, y: Short, w: Short, h: Short): Boolean {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h
    }

    @JvmStatic
    fun isInside(mouseX: Int, mouseY: Int, x: Byte, y: Byte, w: Byte, h: Byte): Boolean {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h
    }
}
