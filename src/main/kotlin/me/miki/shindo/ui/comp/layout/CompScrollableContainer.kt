package me.miki.shindo.ui.comp.layout

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import java.awt.Color

/**
 * Container scrollável vertical com estilo padrão do client.
 * Usado em várias scenes do mod menu.
 */
open class CompScrollableContainer(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : Comp(x, y) {

    private val scroll = Scroll()
    private var radius: Float = 12f
    private var shadowStrength: Int = 7
    private var innerPadding: Float = 18f
    private var contentHeight: Float = 0f

    init {
        setWidth(width)
        setHeight(height)
    }

    fun setRadius(radius: Float): CompScrollableContainer {
        this.radius = radius
        return this
    }

    fun setShadowStrength(strength: Int): CompScrollableContainer {
        this.shadowStrength = strength
        return this
    }

    fun setInnerPadding(padding: Float): CompScrollableContainer {
        this.innerPadding = padding
        return this
    }

    fun getScroll(): Scroll = scroll

    fun setContentHeight(height: Float) {
        this.contentHeight = height
        scroll.maxScroll = kotlin.math.max(0f, contentHeight - getHeight())
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return

        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent

        // Container principal
        nvgInstance.drawShadow(getX(), getY(), getWidth(), getHeight(), radius, shadowStrength)
        nvgInstance.drawRoundedRect(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            radius,
            ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.DARK), 210)
        )
        nvgInstance.drawRoundedRect(
            getX() + 1f,
            getY() + 1f,
            getWidth() - 2f,
            getHeight() - 2f,
            radius - 1f,
            ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.MID), 230)
        )

        // Scroll
        if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            scroll.onScroll()
        }
        scroll.onAnimation()

        val scrollValue = scroll.getValue()

        // Scissor para clipar conteúdo
        nvgInstance.save()
        nvgInstance.scissor(getX(), getY(), getWidth(), getHeight())

        // Renderiza conteúdo customizado (já com scroll aplicado)
        drawScrollableContent(mouseX, mouseY, partialTicks, scrollValue)

        // Renderiza children
        super.draw(mouseX, mouseY, partialTicks)

        nvgInstance.restore()

        // Scrollbar
        nvgInstance.drawScrollbar(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            contentHeight,
            scrollValue,
            paletteColors,
            accentColors,
            24f
        )

        super.draw(mouseX, mouseY, partialTicks)
    }

    /**
     * Callback para renderizar o conteúdo scrollável.
     * Pode ser definido externamente para customização.
     */
    var drawScrollableContent: ((mouseX: Int, mouseY: Int, partialTicks: Float, scrollValue: Float) -> Unit)? = null

    /**
     * Método para renderizar o conteúdo scrollável.
     * @param scrollValue Valor atual do scroll
     */
    protected open fun drawScrollableContent(mouseX: Int, mouseY: Int, partialTicks: Float, scrollValue: Float) {
        drawScrollableContent?.invoke(mouseX, mouseY, partialTicks, scrollValue)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        scroll.onKey(keyCode)
        super.keyTyped(typedChar, keyCode)
    }
}
