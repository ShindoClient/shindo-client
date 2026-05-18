package me.miki.shindo.ui.components.v2

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.utils.mouse.MouseUtils

open class Component(
    private var x: Float = 0f,
    private var y: Float = 0f,
) {
    private var width: Float = 0f
    private var height: Float = 0f
    private var visible: Boolean = true

    private val children: MutableList<Component> = mutableListOf()

    protected val nvg: NanoVGManager
        get() = Shindo.getInstance().nanoVGManager!!

    protected val palette: ColorPalette
        get() = Shindo.getInstance().getColorManager().getPalette()

    protected val accent: AccentColor
        get() = Shindo.getInstance().getColorManager().getCurrentColor()

    protected val colors: ColorManager
        get() = Shindo.getInstance().getColorManager()

    open fun draw(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        if (!visible) return
        drawChildren(mouseX, mouseY, partialTicks)
    }

    open fun update(partialTicks: Float) {
        if (!visible) return
        updateChildren(partialTicks)
    }

    open fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (!visible) return
        forEachChild { it.mouseClicked(mouseX, mouseY, mouseButton) }
    }

    open fun mouseReleased(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (!visible) return
        forEachChild { it.mouseReleased(mouseX, mouseY, mouseButton) }
    }

    open fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        if (!visible) return
        forEachChild { it.keyTyped(typedChar, keyCode) }
    }

    protected open fun drawChildren(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        if (children.isEmpty()) return
        forEachChild { it.draw(mouseX, mouseY, partialTicks) }
    }

    protected open fun updateChildren(partialTicks: Float) {
        if (children.isEmpty()) return
        forEachChild { it.update(partialTicks) }
    }

    open fun getX(): Float = x

    open fun getY(): Float = y

    open fun getWidth(): Float = width

    open fun getHeight(): Float = height

    open fun setX(x: Float) {
        this.x = x
    }

    open fun setY(y: Float) {
        this.y = y
    }

    open fun setWidth(width: Float) {
        this.width = width
    }

    open fun setHeight(height: Float) {
        this.height = height
    }

    open fun setBounds(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    open fun isVisible(): Boolean = visible

    open fun setVisible(visible: Boolean) {
        this.visible = visible
    }

    open fun addChild(component: Component) {
        if (!children.contains(component)) {
            children.add(component)
        }
    }

    open fun removeChild(component: Component) {
        children.remove(component)
    }

    open fun clearChildren() {
        children.clear()
    }

    open fun getChildren(): List<Component> = children.toList()

    open fun hasChildren(): Boolean = children.isNotEmpty()

    open fun isHovered(
        mouseX: Int,
        mouseY: Int,
    ): Boolean = MouseUtils.isInside(mouseX, mouseY, x, y, width, height)

    private inline fun forEachChild(action: (Component) -> Unit) {
        children.forEach(action)
    }
}