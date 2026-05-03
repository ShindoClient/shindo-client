package me.miki.shindo.ui.components.v2

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.ui.components.v2.base.IBounded
import me.miki.shindo.ui.components.v2.base.IComponent
import me.miki.shindo.ui.components.v2.base.IContainer
import me.miki.shindo.ui.components.v2.base.ILayered
import me.miki.shindo.utils.mouse.MouseUtils

/**
 * Base class for all UI components in the ShindoClient component system v2.
 *
 * Implements [IComponent], [IBounded], [IContainer], and [ILayered].
 * Extend this class to create custom components — override [onDraw] for rendering
 * and [onUpdate] for per-frame logic. Input events bubble down to children automatically.
 *
 * PERFORMANCE NOTES:
 * - Called every frame (60 FPS target)
 * - Children are iterated with a plain while-loop (zero iterator allocations)
 * - Manager references are lazily cached per instance
 * - Thread: Main thread only
 *
 * EXTENSION PATTERN:
 * ```kotlin
 * class MyPanel(x: Float, y: Float) : Component(x, y) {
 *     override fun onDraw(mouseX: Int, mouseY: Int, partialTicks: Float) {
 *         // draw background, then call super to draw children
 *         super.onDraw(mouseX, mouseY, partialTicks)
 *     }
 * }
 * ```
 */
open class Component(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : IComponent, IBounded, IContainer, ILayered {

    // ------------------------------------------------------------------ bounds

    private var _x: Float = x
    private var _y: Float = y
    private var _width: Float = width
    private var _height: Float = height

    // ------------------------------------------------------------ padding/gap

    /** Space between this component's edges and its children. */
    var paddingX: Float = 0f
    var paddingY: Float = 0f

    // ---------------------------------------------------------------- z-order

    private var _zIndex: Int = 0

    // --------------------------------------------------------------- children

    /**
     * Children stored as [Component] (not just [IComponent]) so we can
     * propagate z-sorting and access concrete fields without casting.
     */
    private val children: MutableList<Component> = mutableListOf()

    // --------------------------------------------------------------- state

    private var _visible: Boolean = true
    private var _initialised: Boolean = false

    // --------------------------------------------------------- lazy managers

    // Cached per-instance so we avoid repeated getInstance() calls each frame.
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

    // ====================================================== lifecycle (public)

    /** Final draw dispatcher — do not override. Override [onDraw] instead. */
    final override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!_visible) return
        ensureInitialised()
        onDraw(mouseX, mouseY, partialTicks)
    }

    /** Final update dispatcher — do not override. Override [onUpdate] instead. */
    final override fun update(partialTicks: Float) {
        if (!_visible) return
        ensureInitialised()
        onUpdate(partialTicks)
        dispatchUpdateChildren(partialTicks)
    }

    override fun onInit() {}
    override fun onDestroy() {
        forEachChild { it.onDestroy() }
        children.clear()
        _nvg = null
        _palette = null
        _colors = null
        _initialised = false
    }

    // ====================================================== hooks to override

    /**
     * Override to implement component-specific rendering.
     * Call `super.onDraw(...)` at the end to render children on top.
     */
    protected open fun onDraw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        dispatchDrawChildren(mouseX, mouseY, partialTicks)
    }

    /**
     * Override to implement per-frame state changes (animations, timers, etc.).
     * Children are updated automatically after this returns.
     */
    protected open fun onUpdate(partialTicks: Float) {}

    // ====================================================== input dispatching

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!_visible) return
        onMouseClicked(mouseX, mouseY, mouseButton)
        forEachChild { it.mouseClicked(mouseX, mouseY, mouseButton) }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!_visible) return
        onMouseReleased(mouseX, mouseY, mouseButton)
        forEachChild { it.mouseReleased(mouseX, mouseY, mouseButton) }
    }

    override fun mouseScrolled(mouseX: Int, mouseY: Int, delta: Int) {
        if (!_visible) return
        onMouseScrolled(mouseX, mouseY, delta)
        forEachChild { it.mouseScrolled(mouseX, mouseY, delta) }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!_visible) return
        onKeyTyped(typedChar, keyCode)
        forEachChild { it.keyTyped(typedChar, keyCode) }
    }

    /** Override to react to mouse clicks on this component. */
    protected open fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {}

    /** Override to react to mouse releases on this component. */
    protected open fun onMouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {}

    /** Override to react to scroll events on this component. */
    protected open fun onMouseScrolled(mouseX: Int, mouseY: Int, delta: Int) {}

    /** Override to react to key events on this component. */
    protected open fun onKeyTyped(typedChar: Char, keyCode: Int) {}

    // ====================================================== IBounded

    override fun getX(): Float = _x
    override fun getY(): Float = _y
    override fun getWidth(): Float = _width
    override fun getHeight(): Float = _height

    override fun setX(x: Float) { _x = x }
    override fun setY(y: Float) { _y = y }
    override fun setWidth(width: Float) { _width = width }
    override fun setHeight(height: Float) { _height = height }

    override fun setBounds(x: Float, y: Float, width: Float, height: Float) {
        _x = x; _y = y; _width = width; _height = height
    }

    /** Returns true when (mouseX, mouseY) is inside this component's bounds. */
    open fun isHovered(mouseX: Int, mouseY: Int): Boolean =
        MouseUtils.isInside(mouseX, mouseY, _x, _y, _width, _height)

    // ====================================================== IContainer

    /**
     * Adds a child component. Children are kept sorted by z-index (ascending).
     * Only [Component] instances are accepted; IComponent-only objects are ignored.
     */
    override fun addChild(component: IComponent) {
        if (component !is Component) return
        if (children.contains(component)) return
        children.add(component)
        sortChildrenByZ()
        if (_initialised) component.ensureInitialised()
    }

    override fun removeChild(component: IComponent) {
        if (component is Component && children.remove(component)) {
            component.onDestroy()
        }
    }

    override fun clearChildren() {
        forEachChild { it.onDestroy() }
        children.clear()
    }

    override fun getChildren(): List<IComponent> = children

    override fun hasChildren(): Boolean = children.isNotEmpty()

    // ====================================================== ILayered

    override fun getZIndex(): Int = _zIndex

    override fun setZIndex(z: Int) {
        _zIndex = z
        // Notify parent to re-sort (parent handles its own list)
    }

    // ====================================================== IComponent — visibility

    override fun isVisible(): Boolean = _visible

    override fun setVisible(visible: Boolean) {
        _visible = visible
    }

    // ====================================================== internal helpers

    private fun ensureInitialised() {
        if (_initialised) return
        _initialised = true
        onInit()
        forEachChild { it.ensureInitialised() }
    }

    private fun sortChildrenByZ() {
        children.sortBy { it._zIndex }
    }

    private fun dispatchDrawChildren(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (children.isEmpty()) return
        forEachChild { it.draw(mouseX, mouseY, partialTicks) }
    }

    private fun dispatchUpdateChildren(partialTicks: Float) {
        if (children.isEmpty()) return
        forEachChild { it.update(partialTicks) }
    }

    /**
     * Zero-allocation child iterator.
     * Uses index-based while loop to avoid creating an [Iterator] object each frame.
     */
    private inline fun forEachChild(action: (Component) -> Unit) {
        var i = 0
        val size = children.size
        while (i < size) {
            action(children[i++])
        }
    }
}
