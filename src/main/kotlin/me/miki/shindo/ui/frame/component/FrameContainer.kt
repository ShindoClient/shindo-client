package me.miki.shindo.ui.frame.component

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.ColorUtils

/**
 * Container do frame onde os componentes são renderizados.
 * Gerencia o layout e renderização dos componentes anexados.
 */
class FrameContainer(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 400f,
    height: Float = 268f
) : Comp(x, y) {
    
    private var padding: Float = 14f
    private var scrollable: Boolean = false
    
    // Cache
    private var _palette: me.miki.shindo.management.color.palette.ColorPalette? = null
    
    private val palette: me.miki.shindo.management.color.palette.ColorPalette
        get() = _palette ?: Shindo.getInstance().colorManager.palette.also { _palette = it }
    
    init {
        setWidth(width)
        setHeight(height)
    }
    
    fun setPadding(padding: Float): FrameContainer {
        this.padding = padding
        return this
    }
    
    fun setScrollable(scrollable: Boolean): FrameContainer {
        this.scrollable = scrollable
        return this
    }
    
    fun getPadding(): Float = padding
    fun isScrollable(): Boolean = scrollable
    
    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return
        
        val nvgInstance = nvg
        val paletteColors = palette
        
        // Aplica scissor para clipar conteúdo
        nvgInstance.save()
        nvgInstance.scissor(getX(), getY(), getWidth(), getHeight())
        
        // Desenha fundo do container (opcional, pode ser transparente)
        // drawContainerBackground(nvgInstance, paletteColors)
        
        // Renderiza componentes filhos
        super.draw(mouseX, mouseY, partialTicks)
        
        nvgInstance.restore()
    }
    
    /**
     * Desenha fundo do container (opcional).
     */
    private fun drawContainerBackground(
        nvg: me.miki.shindo.management.nanovg.NanoVGManager,
        palette: me.miki.shindo.management.color.palette.ColorPalette
    ) {
        val bgColor = ColorUtils.applyAlpha(
            palette.getBackgroundColor(ColorType.DARK),
            240
        )
        nvg.drawRoundedRectVarying(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            0f,  // top-left
            0f,  // top-right
            12f, // bottom-right
            12f, // bottom-left
            bgColor
        )
    }
    
    /**
     * Obtém a área disponível para conteúdo (considerando padding).
     */
    fun getContentArea(): ContentArea {
        return ContentArea(
            x = getX() + padding,
            y = getY() + padding,
            width = getWidth() - padding * 2f,
            height = getHeight() - padding * 2f
        )
    }
    
    /**
     * Área de conteúdo com padding aplicado.
     */
    data class ContentArea(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float
    )
}
