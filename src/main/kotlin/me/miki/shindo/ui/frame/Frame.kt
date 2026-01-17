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
    
    // Componentes principais (privados para evitar conflito com getters)
    private lateinit var _header: FrameHeader
    private lateinit var _container: FrameContainer
    
    // Estado (privadas para evitar conflito com getters)
    private var _x: Float = 0f
    private var _y: Float = 0f
    private var _width: Float = 400f
    private var _height: Float = 300f
    private var _visible: Boolean = true
    private var _closable: Boolean = true
    private var _usingHeader: Boolean = false // Por padrão, não usa header
    
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
        if (_usingHeader) {
            initHeader()
        }
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
        _x = (sr.scaledWidth - _width) / 2f
        _y = (sr.scaledHeight - _height) / 2f
    }
    
    /**
     * Inicializa o header do frame.
     */
    private fun initHeader() {
        _header = FrameHeader(
            x = getX(),
            y = getY(),
            width = getWidth(),
            title = getTitle()
        ).apply {
            setClosable(isClosable())
            setOnClose { onClose() }
            onHeaderInitialized(this)
        }
    }
    
    /**
     * Inicializa o container do frame.
     */
    private fun initContainer() {
        val headerHeight = if (_usingHeader && ::_header.isInitialized) _header.getHeight() else 0f
        _container = FrameContainer(
            x = getX(),
            y = getY() + headerHeight,
            width = getWidth(),
            height = getHeight() - headerHeight
        ).apply {
            onContainerInitialized(this)
        }
    }
    
    /**
     * Renderiza o frame.
     */
    open fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return
        
        val nvgInstance = nvg
        val paletteColors = palette
        
        // Desenha fundo do frame
        drawFrameBackground(nvgInstance, paletteColors)
        
        // Desenha header (se estiver sendo usado)
        if (_usingHeader && ::_header.isInitialized) {
            _header.draw(mouseX, mouseY, partialTicks)
        }
        
        // Desenha container
        _container.draw(mouseX, mouseY, partialTicks)
        
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
        nvg.drawShadow(getX(), getY(), getWidth(), getHeight(), 12f, 8)
        nvg.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 12f, bgColor)
    }
    
    /**
     * Desenha conteúdo customizado do frame.
     * Sobrescreva este método para adicionar conteúdo específico.
     */
    protected open fun drawContent(mouseX: Int, mouseY: Int, partialTicks: Float) {}
    
    /**
     * Método auxiliar para desenhar conteúdo com translate dentro do container.
     * Garante que o translate funcione corretamente com o scissor do container.
     * 
     * Uso:
     * ```kotlin
     * frame.drawInContainer(0f, scrollValue) { nvg ->
     *     nvg.drawText("Hello", 10f, 10f, ...)
     * }
     * ```
     */
    fun drawInContainer(translateX: Float, translateY: Float, block: (NanoVGManager) -> Unit) {
        _container.drawWithTranslate(translateX, translateY, block)
    }
    
    /**
     * Obtém o contexto NanoVG para uso direto (útil para translate/scissor customizados).
     * Use com cuidado e sempre salve/restaure o estado quando necessário.
     */
    fun getNanoVG(): NanoVGManager = nvg
    
    /**
     * Processa cliques do mouse.
     */
    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible()) return
        
        if (_usingHeader && ::_header.isInitialized) {
            _header.mouseClicked(mouseX, mouseY, mouseButton)
        }
        _container.mouseClicked(mouseX, mouseY, mouseButton)
        onMouseClicked(mouseX, mouseY, mouseButton)
    }
    
    /**
     * Processa soltura do mouse.
     */
    open fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible()) return
        
        if (_usingHeader && ::_header.isInitialized) {
            _header.mouseReleased(mouseX, mouseY, mouseButton)
        }
        _container.mouseReleased(mouseX, mouseY, mouseButton)
        onMouseReleased(mouseX, mouseY, mouseButton)
    }
    
    /**
     * Processa teclas digitadas.
     */
    open fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!isVisible()) return
        
        _container.keyTyped(typedChar, keyCode)
        onKeyTyped(typedChar, keyCode)
        
        // ESC fecha o frame se for closable
        if (keyCode == 1 && isClosable()) { // ESC key
            onClose()
        }
    }
    
    /**
     * Atualiza o frame.
     */
    open fun update(partialTicks: Float) {
        if (!isVisible()) return
        onUpdate(partialTicks)
    }
    
    /**
     * Anexa um componente ao container do frame.
     * Método principal para adicionar componentes de forma prática.
     */
    fun attachToFrame(component: Comp) {
        _container.addChild(component)
    }
    
    /**
     * Anexa múltiplos componentes ao container.
     */
    fun attachToFrame(vararg components: Comp) {
        components.forEach { _container.addChild(it) }
    }
    
    /**
     * Remove um componente do container.
     */
    fun detachFromFrame(component: Comp) {
        _container.removeChild(component)
    }
    
    /**
     * Limpa todos os componentes do container.
     */
    fun clearFrame() {
        _container.clearChildren()
    }
    
    /**
     * Define a posição do frame.
     */
    fun setPosition(x: Float, y: Float) {
        _x = x
        _y = y
        updatePositions()
    }
    
    /**
     * Define o tamanho do frame.
     */
    fun setSize(width: Float, height: Float) {
        _width = width
        _height = height
        updatePositions()
    }
    
    /**
     * Define se o frame é visível.
     */
    fun setVisible(value: Boolean) {
        _visible = value
    }
    
    /**
     * Define se o frame pode ser fechado.
     */
    fun setClosable(value: Boolean) {
        _closable = value
        if (_usingHeader && ::_header.isInitialized) {
            _header.setClosable(value)
        }
    }
    
    /**
     * Define se o frame deve usar header.
     */
    fun setUsingHeader(value: Boolean) {
        _usingHeader = value
    }
    
    /**
     * Verifica se o frame está usando header.
     */
    fun isUsingHeader(): Boolean = _usingHeader
    
    /**
     * Atualiza posições dos componentes quando o frame é movido/redimensionado.
     */
    private fun updatePositions() {
        if (_usingHeader && ::_header.isInitialized) {
            _header.setX(getX())
            _header.setY(getY())
            _header.setWidth(getWidth())
        }
        if (::_container.isInitialized) {
            val headerHeight = if (_usingHeader && ::_header.isInitialized) _header.getHeight() else 0f
            _container.setX(getX())
            _container.setY(getY() + headerHeight)
            _container.setWidth(getWidth())
            _container.setHeight(getHeight() - headerHeight)
        }
    }
    
    /**
     * Fecha o frame.
     */
    fun close() {
        onClose()
    }
    
    // Getters
    fun getX(): Float = _x
    fun getY(): Float = _y
    fun getWidth(): Float = _width
    fun getHeight(): Float = _height
    fun isVisible(): Boolean = _visible
    fun isClosable(): Boolean = _closable

    fun getHeader(): FrameHeader? = if (_usingHeader && ::_header.isInitialized) _header else null
    fun getContainer(): FrameContainer = _container
    
    /**
     * Obtém a altura do header (0 se não estiver sendo usado).
     */
    fun getHeaderHeight(): Float = if (_usingHeader && ::_header.isInitialized) _header.getHeight() else 0f
    
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
        _visible = false
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
