package me.miki.shindo.gui.widget

import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.types.Rect
import me.miki.shindo.types.Size


abstract class Widget(private var x: Float, private var y: Float, private var width: Float, private var height: Float) {
    private val bounds: Rect = Rect()
    private fun updateBounds() {
        bounds[x, y, width] = height
    }

    abstract fun render(renderer: NanoVGManager, mouseX: Float, mouseY: Float)
    abstract fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean
    abstract fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean
    fun isHovered(mouseX: Float, mouseY: Float): Boolean {
        return bounds.contains(mouseX, mouseY)
    }

    fun getX(): Float {
        return x
    }

    fun setX(x: Float) {
        this.x = x
        updateBounds()
    }

    fun getY(): Float {
        return y
    }

    fun setY(y: Float) {
        this.y = y
        updateBounds()
    }

    fun getWidth(): Float {
        return width
    }

    fun setWidth(width: Float) {
        this.width = width
        updateBounds()
    }

    fun getHeight(): Float {
        return height
    }

    fun setHeight(height: Float) {
        this.height = height
        updateBounds()
    }

    fun getBounds(): Rect {
        return bounds
    }

    fun setPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
        updateBounds()
    }

    fun setSize(width: Float, height: Float) {
        this.width = width
        this.height = height
        updateBounds()
    }

    fun setSize(size: Size) {
        setSize(size.width, size.height)
    }

    init {
        updateBounds()
    }
}