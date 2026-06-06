package com.shindoclient.shindo.ui.components.v2.templates

import com.shindoclient.shindo.ui.components.v2.Component

open class CompContainer(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f,
) : Component(x, y) {
    private var padding: Float = 0f
    private var spacing: Float = 0f
    private var layoutDirection: LayoutDirection = LayoutDirection.VERTICAL

    init {
        setWidth(width)
        setHeight(height)
    }

    enum class LayoutDirection {
        VERTICAL,
        HORIZONTAL,
    }

    fun setPadding(padding: Float): CompContainer {
        this.padding = padding
        return this
    }

    fun setSpacing(spacing: Float): CompContainer {
        this.spacing = spacing
        return this
    }

    fun setLayoutDirection(direction: LayoutDirection): CompContainer {
        this.layoutDirection = direction
        return this
    }

    override fun draw(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        if (!isVisible()) return

        updateLayout()
        super.draw(mouseX, mouseY, partialTicks)
    }

    protected open fun updateLayout() {
        val children = getChildren()
        if (children.isEmpty()) return

        var currentX = getX() + padding
        var currentY = getY() + padding

        for (child in children) {
            child.setX(currentX)
            child.setY(currentY)

            when (layoutDirection) {
                LayoutDirection.VERTICAL -> {
                    currentY += child.getHeight() + spacing
                }

                LayoutDirection.HORIZONTAL -> {
                    currentX += child.getWidth() + spacing
                }
            }
        }
    }
}
