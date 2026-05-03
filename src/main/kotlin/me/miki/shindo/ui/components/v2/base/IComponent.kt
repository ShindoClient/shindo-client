package me.miki.shindo.ui.components.v2.base

interface IComponent {

    fun draw(mouseX: Int, mouseY: Int, partialTicks: Float)
    fun update(partialTicks: Float)
    fun onInit() {}
    fun onDestroy() {}
    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int)
    fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int)
    fun mouseScrolled(mouseX: Int, mouseY: Int, delta: Int) {}
    fun keyTyped(typedChar: Char, keyCode: Int)
    fun isVisible(): Boolean
    fun setVisible(visible: Boolean)
}

// ============================================================
// IBounded — Spatial properties (position + size)
// ============================================================

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
    fun containsPoint(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= getX() && mouseX <= getX() + getWidth() &&
                mouseY >= getY() && mouseY <= getY() + getHeight()
    }
}

// ============================================================
// IContainer — Parent-child hierarchy
// ============================================================

interface IContainer {
    fun addChild(component: IComponent)
    fun removeChild(component: IComponent)
    fun clearChildren()
    fun getChildren(): List<IComponent>
    fun hasChildren(): Boolean
    fun addChildren(vararg components: IComponent) {
        for (c in components) addChild(c)
    }
}

// ============================================================
// IInteractive — Hover / click callbacks + enabled state
// ============================================================

interface IInteractive {

    fun isEnabled(): Boolean
    fun setEnabled(enabled: Boolean)
    var onHoverEnter: (() -> Unit)?
    var onHoverExit: (() -> Unit)?
    var onClick: (() -> Unit)?
    var onRelease: (() -> Unit)?
}

// ============================================================
// IAnimatable — Optional animation contract
// ============================================================

interface IAnimatable {

    fun getAnimationProgress(): Float
    fun animateIn()
    fun animateOut()
}

// ============================================================
// ILayered — Z-order support for overlapping components
// ============================================================

interface ILayered {

    fun getZIndex(): Int
    fun setZIndex(z: Int)
}
