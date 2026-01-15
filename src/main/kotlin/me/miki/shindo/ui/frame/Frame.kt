package me.miki.shindo.ui.frame

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.ui.frame.component.FrameContainer
import me.miki.shindo.ui.frame.component.FrameHeader
import me.miki.shindo.utils.ColorUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

/**
 * Classe base abstrata para frames.
 * Frames são janelas modais/flutuantes com header e container.
 * 
 * Estrutura:
 * - Header: título e botão de close
 * - Container: área de conteúdo onde components são renderizados
 */
abstract class Frame(
    protected val parent: GuiScreen? = null
) {
    protected val mc: Minecraft = Minecraft.getMinecraft()
    
    // Componentes principais
    protected lateinit var header: FrameHeader
    protected lateinit var container: FrameContainer
    
    // Estado
    protected var x: Float = 0f
    protected var y: Float = 0f
    protected var width: Float = 400f
    protected var height: Float = 300f
    protected var visible: Boolean = true
    protected var closable: Boolean = true
    
    // Cache de instâncias
    private var _nvg: NanoVGManager? = null
    private var _palette: ColorPalette? = null
    
    protected val nvg: NanoVGManager
        get() = _nvg ?: Shindo.getInstance().nanoVGManager!!.also { _nvg = it }
    
    protected val palette: ColorPalette
        get() = _palette ?: Shindo.getInstance().colorManager.palette.also { _palette = it }
    
    /**
     * Inicializa o frame. Chamado quando o frame é criado.
     */
    open fun init() {
        setupFrame()
        initHeader()
        initContainer()
        onFrameInitialized()
    }
    
    /**
     * Configura dimensões e posição do frame.
     * Pode ser sobrescrito para customização.
     */
    protected open fun setupFrame() {
        val sr = ScaledResolution(mc)
        // Centraliza por padrão
        x = (sr.scaledWidth - width) / 2f
        y = (sr.scaledHeight - height) / 2f
    }
    
    /**
     * Inicializa o header do frame.
     */
    private fun initHeader() {
        header = FrameHeader(
            x = x,
            y = y,
            width = width,
            title = getTitle()
        ).apply {
            setClosable(closable)
            setOnClose { onClose() }
            onHeaderInitialized(this)
        }
    }
    
    /**
     * Inicializa o container do frame.
     */
    private fun initContainer() {
        val headerHeight = header.getHeight()
        container = FrameContainer(
            x = x,
            y = y + headerHeight,
            width = width,
            height = height - headerHeight
        ).apply {
            onContainerInitialized(this)
        }
    }
    
    /**
     * Renderiza o frame.
     */
    open fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible) return
        
        val nvgInstance = nvg
        val paletteColors = palette
        
        // Desenha fundo do frame
        drawFrameBackground(nvgInstance, paletteColors)
        
        // Desenha header
        header.draw(mouseX, mouseY, partialTicks)
        
        // Desenha container
        container.draw(mouseX, mouseY, partialTicks)
        
        // Desenha conteúdo customizado
        drawContent(mouseX, mouseY, partialTicks)
    }
    
    /**
     * Desenha o fundo do frame.
     */
    protected open fun drawFrameBackground(nvg: NanoVGManager, palette: ColorPalette) {
        val bgColor = ColorUtils.applyAlpha(
            palette.getBackgroundColor(ColorType.DARK),
            245
        )
        nvg.drawShadow(x, y, width, height, 12f, 8)
        nvg.drawRoundedRect(x, y, width, height, 12f, bgColor)
    }
    
    /**
     * Desenha conteúdo customizado do frame.
     * Sobrescreva este método para adicionar conteúdo específico.
     */
    protected open fun drawContent(mouseX: Int, mouseY: Int, partialTicks: Float) {}
    
    /**
     * Processa cliques do mouse.
     */
    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!visible) return
        
        header.mouseClicked(mouseX, mouseY, mouseButton)
        container.mouseClicked(mouseX, mouseY, mouseButton)
        onMouseClicked(mouseX, mouseY, mouseButton)
    }
    
    /**
     * Processa soltura do mouse.
     */
    open fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!visible) return
        
        header.mouseReleased(mouseX, mouseY, mouseButton)
        container.mouseReleased(mouseX, mouseY, mouseButton)
        onMouseReleased(mouseX, mouseY, mouseButton)
    }
    
    /**
     * Processa teclas digitadas.
     */
    open fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!visible) return
        
        container.keyTyped(typedChar, keyCode)
        onKeyTyped(typedChar, keyCode)
        
        // ESC fecha o frame se for closable
        if (keyCode == 1 && closable) { // ESC key
            onClose()
        }
    }
    
    /**
     * Atualiza o frame.
     */
    open fun update(partialTicks: Float) {
        if (!visible) return
        onUpdate(partialTicks)
    }
    
    /**
     * Anexa um componente ao container do frame.
     * Método principal para adicionar componentes de forma prática.
     */
    fun attachToFrame(component: Comp) {
        container.addChild(component)
    }
    
    /**
     * Anexa múltiplos componentes ao container.
     */
    fun attachToFrame(vararg components: Comp) {
        components.forEach { container.addChild(it) }
    }
    
    /**
     * Remove um componente do container.
     */
    fun detachFromFrame(component: Comp) {
        container.removeChild(component)
    }
    
    /**
     * Limpa todos os componentes do container.
     */
    fun clearFrame() {
        container.clearChildren()
    }
    
    /**
     * Define a posição do frame.
     */
    fun setPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
        updatePositions()
    }
    
    /**
     * Define o tamanho do frame.
     */
    fun setSize(width: Float, height: Float) {
        this.width = width
        this.height = height
        updatePositions()
    }
    
    /**
     * Define se o frame é visível.
     */
    fun setVisible(visible: Boolean) {
        this.visible = visible
    }
    
    /**
     * Define se o frame pode ser fechado.
     */
    fun setClosable(closable: Boolean) {
        this.closable = closable
        if (::header.isInitialized) {
            header.setClosable(closable)
        }
    }
    
    /**
     * Atualiza posições dos componentes quando o frame é movido/redimensionado.
     */
    private fun updatePositions() {
        if (::header.isInitialized) {
            header.setX(x)
            header.setY(y)
            header.setWidth(width)
        }
        if (::container.isInitialized) {
            val headerHeight = if (::header.isInitialized) header.getHeight() else 0f
            container.setX(x)
            container.setY(y + headerHeight)
            container.setWidth(width)
            container.setHeight(height - headerHeight)
        }
    }
    
    /**
     * Fecha o frame.
     */
    fun close() {
        onClose()
    }
    
    // Getters
    fun getX(): Float = x
    fun getY(): Float = y
    fun getWidth(): Float = width
    fun getHeight(): Float = height
    fun isVisible(): Boolean = visible
    fun isClosable(): Boolean = closable
    fun getHeader(): FrameHeader = header
    fun getContainer(): FrameContainer = container
    
    // Métodos abstratos/hooks
    /**
     * Retorna o título do frame (exibido no header).
     */
    abstract fun getTitle(): String
    
    /**
     * Chamado quando o frame é inicializado.
     */
    protected open fun onFrameInitialized() {}
    
    /**
     * Chamado quando o header é inicializado.
     * Permite customizar o header antes de ser usado.
     */
    protected open fun onHeaderInitialized(header: FrameHeader) {}
    
    /**
     * Chamado quando o container é inicializado.
     * Permite customizar o container antes de ser usado.
     */
    protected open fun onContainerInitialized(container: FrameContainer) {}
    
    /**
     * Chamado quando o frame é fechado.
     */
    protected open fun onClose() {
        visible = false
        parent?.let { mc.displayGuiScreen(it) }
    }
    
    /**
     * Chamado quando o mouse é clicado no frame.
     */
    protected open fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {}
    
    /**
     * Chamado quando o mouse é solto no frame.
     */
    protected open fun onMouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {}
    
    /**
     * Chamado quando uma tecla é digitada no frame.
     */
    protected open fun onKeyTyped(typedChar: Char, keyCode: Int) {}
    
    /**
     * Chamado a cada frame para atualização.
     */
    protected open fun onUpdate(partialTicks: Float) {}
}
