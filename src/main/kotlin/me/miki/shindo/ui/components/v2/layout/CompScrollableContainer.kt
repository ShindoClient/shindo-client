package me.miki.shindo.ui.components.v2.layout

import me.miki.shindo.ui.components.v2.templates.CompPanel
import me.miki.shindo.ui.components.v2.templates.PanelStyle
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import kotlin.math.max

open class CompScrollableContainer(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f,
) : CompPanel(x, y, width, height) {
    data class ScrollViewport(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
    )

    private val scroll = Scroll()
    private var innerPadding = 18f
    private var contentHeight = 0f
    private var scrollbarGutter = 12f
    private var themeScrollbarOnly = false
    private var contentRenderer: ((mouseX: Int, mouseY: Int, partialTicks: Float, scrollValue: Float) -> Unit)? = null
    private var contentRendererWithViewport: (
        (mouseX: Int, mouseY: Int, partialTicks: Float, scrollValue: Float, viewport: ScrollViewport) -> Unit
    )? =
        null
    private var lastViewport = ScrollViewport(0f, 0f, 0f, 0f)

    init {
        setStyle(PanelStyle.PANEL)
        setRadius(12f)
        setShadowStrength(7)
    }

    fun setInnerPadding(padding: Float): CompScrollableContainer {
        this.innerPadding = max(0f, padding)
        return this
    }

    fun setScrollbarGutter(gutter: Float): CompScrollableContainer {
        this.scrollbarGutter = max(0f, gutter)
        return this
    }

    fun setContentHeight(height: Float) {
        contentHeight = max(0f, height)
        updateScrollBounds(lastViewport.height)
    }

    fun setContentRenderer(
        renderer: ((mouseX: Int, mouseY: Int, partialTicks: Float, scrollValue: Float) -> Unit)?,
    ): CompScrollableContainer {
        contentRenderer = renderer
        return this
    }

    fun setContentRendererWithViewport(
        renderer: (
            (mouseX: Int, mouseY: Int, partialTicks: Float, scrollValue: Float, viewport: ScrollViewport) -> Unit
        )?,
    ): CompScrollableContainer {
        contentRendererWithViewport = renderer
        return this
    }

    fun render(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        contentHeight: Float,
        renderer: (mouseX: Int, mouseY: Int, partialTicks: Float, scrollValue: Float) -> Unit,
    ) {
        setContentHeight(contentHeight)
        setContentRenderer(renderer)
        draw(mouseX, mouseY, partialTicks)
    }

    fun renderWithViewport(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        contentHeight: Float,
        renderer: (mouseX: Int, mouseY: Int, partialTicks: Float, scrollValue: Float, viewport: ScrollViewport) -> Unit,
    ) {
        setContentHeight(contentHeight)
        setContentRendererWithViewport(renderer)
        draw(mouseX, mouseY, partialTicks)
    }

    override fun drawPanelContent(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val fullViewport = calculateViewport()
        val needsScrollbar = contentHeight > fullViewport.height
        val viewport =
            if (needsScrollbar) {
                ScrollViewport(
                    fullViewport.x,
                    fullViewport.y,
                    max(0f, fullViewport.width - scrollbarGutter),
                    fullViewport.height,
                )
            } else {
                fullViewport
            }
        lastViewport = viewport

        if (viewport.width <= 0f || viewport.height <= 0f) {
            return
        }

        updateScrollBounds(viewport.height)

        if (scroll.maxScroll > 0f &&
            MouseUtils.isInside(
                mouseX,
                mouseY,
                fullViewport.x,
                fullViewport.y,
                fullViewport.width,
                fullViewport.height,
            )
        ) {
            scroll.onScroll()
        }
        scroll.onAnimation()

        val scrollValue = scroll.getValue()

        nvg.save()
        nvg.scissor(viewport.x, viewport.y, viewport.width, viewport.height)

        drawScrollableContent(mouseX, mouseY, partialTicks, scrollValue, viewport)
        contentRendererWithViewport?.invoke(mouseX, mouseY, partialTicks, scrollValue, viewport)
        contentRenderer?.invoke(mouseX, mouseY, partialTicks, scrollValue)

        nvg.restore()

        nvg.drawScrollbar(
            fullViewport.x,
            fullViewport.y,
            fullViewport.width,
            fullViewport.height,
            contentHeight,
            scrollValue,
            palette,
            accent,
            24f,
        )
    }

    protected open fun drawScrollableContent(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        scrollValue: Float,
        viewport: ScrollViewport,
    ) {
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        if (scroll.maxScroll > 0f) {
            scroll.onKey(keyCode)
        }
        super.keyTyped(typedChar, keyCode)
    }

    private fun calculateViewport(): ScrollViewport {
        val viewportX = getX() + innerPadding
        val viewportY = getY() + innerPadding
        val viewportWidth = max(0f, getWidth() - innerPadding * 2f)
        val viewportHeight = max(0f, getHeight() - innerPadding * 2f)
        return ScrollViewport(viewportX, viewportY, viewportWidth, viewportHeight)
    }

    private fun updateScrollBounds(visibleHeight: Float) {
        val boundedVisible = max(0f, visibleHeight)
        scroll.maxScroll = max(0f, contentHeight - boundedVisible)
    }
}
