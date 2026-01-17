package me.miki.shindo.ui.frame.component

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
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
    
    // Usa os métodos protegidos do Comp
    
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
        
        val nvgInstance = super.nvg
        val paletteColors = super.palette
        
        // Salva o estado atual do contexto (incluindo translate, scissor, etc)
        nvgInstance.save()
        
        // Aplica scissor para clipar conteúdo (em coordenadas absolutas)
        nvgInstance.scissor(getX(), getY(), getWidth(), getHeight())
        
        // Desenha fundo do container (opcional, pode ser transparente)
        // drawContainerBackground(nvgInstance, paletteColors)
        
        // Renderiza componentes filhos
        // Nota: translate pode ser usado dentro deste bloco e será respeitado pelo scissor
        super.draw(mouseX, mouseY, partialTicks)
        
        // Restaura o estado do contexto (remove translate, scissor, etc)
        nvgInstance.restore()
    }
    
    /**
     * Método auxiliar para desenhar conteúdo com translate e scissor.
     * Garante que o scissor seja aplicado corretamente mesmo com translate.
     * 
     * Uso:
     * ```kotlin
     * container.drawWithTranslate(0f, scrollValue) { nvg ->
     *     // Desenha conteúdo aqui
     *     nvg.drawText("Hello", 10f, 10f, ...)
     * }
     * ```
     */
    fun drawWithTranslate(translateX: Float, translateY: Float, block: (NanoVGManager) -> Unit) {
        if (!isVisible()) return
        
        val nvgInstance = super.nvg
        nvgInstance.save()
        
        // Aplica scissor primeiro (em coordenadas absolutas)
        nvgInstance.scissor(getX(), getY(), getWidth(), getHeight())
        
        // Depois aplica translate
        nvgInstance.translate(translateX, translateY)
        
        // Executa o bloco de desenho com acesso ao nvg
        block(nvgInstance)
        
        // Restaura tudo
        nvgInstance.restore()
    }
    
    /**
     * Método auxiliar para desenhar conteúdo com translate, scissor e animação.
     * Útil para criar seções animadas dentro do frame.
     * 
     * Uso:
     * ```kotlin
     * val animation = SimpleAnimation()
     * animation.setAnimation(1f, 20.0)
     * container.drawWithAnimation(0f, animation.value) { nvg ->
     *     // Desenha conteúdo animado aqui
     * }
     * ```
     */
    fun drawWithAnimation(translateX: Float, translateY: Float, block: (NanoVGManager) -> Unit) {
        drawWithTranslate(translateX, translateY, block)
    }
    
    /**
     * Desenha fundo do container (opcional).
     */
    private fun drawContainerBackground(
        nvg: NanoVGManager,
        palette: ColorPalette
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
