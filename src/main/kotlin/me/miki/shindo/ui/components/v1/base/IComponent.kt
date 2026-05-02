package me.miki.shindo.ui.components.v1.base

// === Core Component Interface ===
interface IComponent {
    fun draw(mouseX: Int, mouseY: Int, partialTicks: Float)
    fun update(partialTicks: Float)
    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int)
    fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int)
    fun keyTyped(typedChar: Char, keyCode: Int)
    fun isVisible(): Boolean
    fun setVisible(visible: Boolean)
}

// === Bounding Box Interface ===
interface IBounded {
    fun getX(): Float
    fun setX(x: Float)
    fun getY(): Float
    fun setY(y: Float)
    fun getWidth(): Float
    fun setWidth(width: Float)
    fun getHeight(): Float
    fun setHeight(height: Float)
    fun setBounds(x: Float, y: Float, width: Float, height: Float) {
        setX(x)
        setY(y)
        setWidth(width)
        setHeight(height)
    }
}

// === Container Interface ===
interface IContainer {
    fun addChild(component: IComponent?)
    fun removeChild(component: IComponent)
    fun clearChildren()
    fun getChildren(): List<IComponent>
    fun hasChildren(): Boolean
}

// === Interactive Interface ===
interface IInteractive {
    fun isHovered(mouseX: Int, mouseY: Int): Boolean
    fun isEnabled(): Boolean
    fun setEnabled(enabled: Boolean)
    var onClick: (() -> Unit)?
    var onHoverEnter: (() -> Unit)?
    var onHoverExit: (() -> Unit)?
}