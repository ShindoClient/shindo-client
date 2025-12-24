package me.miki.shindo.ui.comp

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.utils.mouse.MouseUtils

open class Comp(
    private var x: Float,
    private var y: Float
) {

    private var width: Float = 0f
    private var height: Float = 0f
    private var isVisible: Boolean = true

    private val children: MutableList<Comp> = mutableListOf()

    protected val nvg: NanoVGManager
        get() = Shindo.getInstance().nanoVGManager!!

    protected val palette: ColorPalette
        get() = Shindo.getInstance().colorManager.palette

    protected val accent: AccentColor
        get() = Shindo.getInstance().colorManager.currentColor

    protected val colors: ColorManager
        get() = Shindo.getInstance().colorManager

    open fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible) return
        drawChildren(mouseX, mouseY, partialTicks)
    }

    open fun update(partialTicks: Float) {}

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible) return
        forEachChild { it.mouseClicked(mouseX, mouseY, mouseButton) }
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible) return
        forEachChild { it.mouseReleased(mouseX, mouseY, mouseButton) }
    }

    open fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!isVisible) return
        forEachChild { it.keyTyped(typedChar, keyCode) }
    }

    protected open fun drawChildren(mouseX: Int, mouseY: Int, partialTicks: Float) {
        forEachChild { it.draw(mouseX, mouseY, partialTicks) }
    }

    fun addChild(comp: Comp?) {
        if (comp != null && !children.contains(comp)) {
            children.add(comp)
        }
    }

    fun removeChild(comp: Comp) {
        children.remove(comp)
    }

    fun clearChildren() {
        children.clear()
    }

    fun children(): List<Comp> = children.toList()

    fun hasChildren(): Boolean = children.isNotEmpty()


    open fun getX(): Float = x
    open fun getY(): Float = y
    open fun getWidth(): Float = width
    open fun getHeight(): Float = height
    open fun isVisible(): Boolean = isVisible

    open fun setX(x: Float) { this.x = x }
    open fun setY(y: Float) { this.y = y }
    open fun setWidth(width: Float) { this.width = width }
    open fun setHeight(height: Float) { this.height = height }
    open fun setVisible(isVisible: Boolean) { this.isVisible = isVisible }

    fun setBounds(x: Float, y: Float, width: Float, height: Float) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    fun isHovered(mouseX: Int, mouseY: Int): Boolean =
        MouseUtils.isInside(mouseX, mouseY, x, y, width, height)

    private inline fun forEachChild(action: (Comp) -> Unit) {
        children.forEach(action)
    }
}