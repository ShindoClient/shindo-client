package me.miki.shindo.ui.components.v2.templates

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.components.v2.Component
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import org.lwjgl.input.Mouse
import java.awt.Color

open class CompScrollable(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : Component(x, y) {

    private var scrollY: Float = 0f
    private var contentHeight: Float = 0f
    private var scrollbarWidth: Float = 6f
    private var scrollbarEnabled: Boolean = true
    private var scrollbarColor: Color? = null
    private var smoothScrolling: Boolean = true
    private var scrollSpeed: Float = 15f

    private var isDraggingScrollbar: Boolean = false
    private var lastMouseY: Int = 0

    init {
        setWidth(width)
        setHeight(height)
    }

    fun getScrollY(): Float = scrollY
    fun setScrollY(value: Float) {
        scrollY = value.coerceIn(0f, getMaxScroll())
    }

    fun getContentHeight(): Float = contentHeight

    fun setContentHeight(height: Float) {
        this.contentHeight = height
        scrollY = scrollY.coerceIn(0f, getMaxScroll())
    }

    fun setScrollbarWidth(width: Float): CompScrollable {
        this.scrollbarWidth = width
        return this
    }

    fun setScrollbarEnabled(enabled: Boolean): CompScrollable {
        this.scrollbarEnabled = enabled
        return this
    }

    fun setScrollbarColor(color: Color?): CompScrollable {
        this.scrollbarColor = color
        return this
    }

    fun setSmoothScrolling(enabled: Boolean): CompScrollable {
        this.smoothScrolling = enabled
        return this
    }

    fun setScrollSpeed(speed: Float): CompScrollable {
        this.scrollSpeed = speed
        return this
    }

    fun scrollToTop() {
        scrollY = 0f
    }

    fun scrollToBottom() {
        scrollY = getMaxScroll()
    }

    fun scrollBy(delta: Float) {
        setScrollY(scrollY - delta)
    }

    private fun getMaxScroll(): Float = (contentHeight - getHeight()).coerceAtLeast(0f)

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return

        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent

        nvgInstance.save()
        nvgInstance.scissor(getX(), getY(), getWidth(), getHeight())

        nvgInstance.save()
        nvgInstance.translate(0f, -scrollY)

        drawScrollableContent(mouseX, mouseY, partialTicks)

        nvgInstance.restore()

        if (scrollbarEnabled && contentHeight > getHeight()) {
            val scrollbarX = getX() + getWidth() - scrollbarWidth - 4f
            val scrollbarY = getY() + 4f
            val scrollbarHeight = getHeight() - 8f

            val trackColor = ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.NORMAL), 130)
            nvgInstance.drawRoundedRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 2f, trackColor)

            val visibleRatio = (getHeight() / contentHeight).coerceIn(0.1f, 1f)
            val handleHeight = scrollbarHeight * visibleRatio
            val maxScroll = getMaxScroll()
            val handleY = scrollbarY + (scrollbarHeight - handleHeight) * (scrollY / maxScroll.coerceAtLeast(1f))

            val handleColor = scrollbarColor ?: ColorUtils.applyAlpha(accentColors.getColor1(), 190)
            nvgInstance.drawRoundedRect(scrollbarX, handleY, scrollbarWidth, handleHeight, 2f, handleColor)
        }

        nvgInstance.restore()
        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible()) return

        if (scrollbarEnabled && contentHeight > getHeight()) {
            val scrollbarX = getX() + getWidth() - scrollbarWidth - 4f
            val scrollbarY = getY() + 4f
            val scrollbarHeight = getHeight() - 8f

            if (MouseUtils.isInside(mouseX, mouseY, scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight)) {
                if (mouseButton == 0) {
                    isDraggingScrollbar = true
                    lastMouseY = mouseY
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        isDraggingScrollbar = false
        super.mouseReleased(mouseX, mouseY, mouseButton)
    }

    override fun update(partialTicks: Float) {
        if (isDraggingScrollbar) {
            val deltaY = (lastMouseY - Mouse.getY()) * 0.5f
            scrollBy(deltaY)
            lastMouseY = Mouse.getY()
        }

        val wheel = Mouse.getDWheel()
        if (wheel != 0 && MouseUtils.isInside(
                Mouse.getX() * 2,
                Mouse.getY() * 2,
                getX(),
                getY(),
                getWidth(),
                getHeight()
            )
        ) {
            scrollBy(wheel / 120f * scrollSpeed)
        }

        super.update(partialTicks)
    }

    protected open fun drawScrollableContent(mouseX: Int, mouseY: Int, partialTicks: Float) {}
}
