package me.miki.shindo.ui.comp

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.ui.comp.base.IBounded
import me.miki.shindo.ui.comp.base.IComponent
import me.miki.shindo.ui.comp.base.IContainer
import me.miki.shindo.utils.mouse.MouseUtils


open class Comp(
    x: Float = 0f,
    y: Float = 0f
) : IComponent, IBounded, IContainer {

    private var _x: Float = x
    private var _y: Float = y
    private var _width: Float = 0f
    private var _height: Float = 0f
    private var _visible: Boolean = true

    private val children: MutableList<Comp> = mutableListOf()

    private var _nvg: NanoVGManager? = null
    private var _palette: ColorPalette? = null
    private var _colors: ColorManager? = null

    protected val nvg: NanoVGManager
        get() = _nvg ?: Shindo.getInstance().nanoVGManager!!.also { _nvg = it }

    protected val palette: ColorPalette
        get() = _palette ?: Shindo.getInstance().colorManager.getPalette().also { _palette = it }

    protected val accent: AccentColor
        get() = Shindo.getInstance().colorManager.getCurrentColor()

    protected val colors: ColorManager
        get() = _colors ?: Shindo.getInstance().colorManager.also { _colors = it }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!_visible) return
        drawChildren(mouseX, mouseY, partialTicks)
    }

    override fun update(partialTicks: Float) {
        if (!_visible) return
        updateChildren(partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!_visible) return
        forEachChild { it.mouseClicked(mouseX, mouseY, mouseButton) }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!_visible) return
        forEachChild { it.mouseReleased(mouseX, mouseY, mouseButton) }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!_visible) return
        forEachChild { it.keyTyped(typedChar, keyCode) }
    }

    protected open fun drawChildren(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (children.isEmpty()) return
        forEachChild { it.draw(mouseX, mouseY, partialTicks) }
    }

    protected open fun updateChildren(partialTicks: Float) {
        if (children.isEmpty()) return
        forEachChild { it.update(partialTicks) }
    }

    override fun getX(): Float = _x
    override fun getY(): Float = _y
    override fun getWidth(): Float = _width
    override fun getHeight(): Float = _height

    override fun setX(x: Float) {
        this._x = x
    }

    override fun setY(y: Float) {
        this._y = y
    }

    override fun setWidth(width: Float) {
        this._width = width
    }

    override fun setHeight(height: Float) {
        this._height = height
    }

    override fun setBounds(x: Float, y: Float, width: Float, height: Float) {
        this._x = x
        this._y = y
        this._width = width
        this._height = height
    }

    override fun isVisible(): Boolean = _visible
    override fun setVisible(visible: Boolean) {
        this._visible = visible
    }

    override fun addChild(component: IComponent?) {
        if (component is Comp && !children.contains(component)) {
            children.add(component)
        }
    }

    override fun removeChild(component: IComponent) {
        if (component is Comp) {
            children.remove(component)
        }
    }

    override fun clearChildren() {
        children.clear()
    }

    override fun getChildren(): List<IComponent> = children.toList()

    override fun hasChildren(): Boolean = children.isNotEmpty()

    open fun isHovered(mouseX: Int, mouseY: Int): Boolean =
        MouseUtils.isInside(mouseX, mouseY, _x, _y, _width, _height)

    private inline fun forEachChild(action: (Comp) -> Unit) {
        val size = children.size
        var i = 0
        while (i < size) {
            action(children[i])
            i++
        }
    }

    @Deprecated("Use getChildren() instead", ReplaceWith("getChildren()"))
    fun children(): List<Comp> = children.toList()
}