package me.miki.shindo.ui.frame

import me.miki.shindo.Shindo
import me.miki.shindo.ui.frame.template.FrameTemplate
import net.minecraft.client.gui.GuiScreen
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Gerenciador central de frames.
 * Permite gerenciar múltiplos frames abertos simultaneamente.
 */
object FrameManager {
    private val activeFrames: MutableList<Frame> = CopyOnWriteArrayList()
    
    /**
     * Abre um frame e adiciona à lista de frames ativos.
     */
    fun openFrame(frame: Frame) {
        if (!activeFrames.contains(frame)) {
            activeFrames.add(frame)
            frame.init()
        }
    }
    
    /**
     * Cria e abre um frame usando um template.
     */
    fun createAndOpenFrame(
        template: FrameTemplate,
        parent: GuiScreen? = null,
        title: String = ""
    ): Frame {
        val frame = template.createFrame(parent, title)
        openFrame(frame)
        return frame
    }
    
    /**
     * Fecha um frame específico.
     */
    fun closeFrame(frame: Frame) {
        activeFrames.remove(frame)
    }
    
    /**
     * Fecha todos os frames ativos.
     */
    fun closeAllFrames() {
        activeFrames.clear()
    }

    /**
     * Renderiza todos os frames ativos.
     */
    @JvmStatic
    fun drawFrames(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (activeFrames.isEmpty()) return

        val nvg = Shindo.getInstance().nanoVGManager!!
        nvg.setupAndDraw {
            activeFrames.forEach { frame ->
                if (frame.isVisible()) {
                    frame.draw(mouseX, mouseY, partialTicks)
                }
            }
        }
    }

    /**
     * Processa cliques do mouse em todos os frames.
     */
    @JvmStatic
    fun handleMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // Processa do último para o primeiro (frames no topo primeiro)
        activeFrames.reversed().forEach { frame ->
            if (frame.isVisible()) {
                frame.mouseClicked(mouseX, mouseY, mouseButton)
            }
        }
    }

    /**
     * Processa soltura do mouse em todos os frames.
     */
    @JvmStatic
    fun handleMouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        activeFrames.reversed().forEach { frame ->
            if (frame.isVisible()) {
                frame.mouseReleased(mouseX, mouseY, mouseButton)
            }
        }
    }

    /**
     * Processa teclas digitadas em todos os frames.
     */
    @JvmStatic
    fun handleKeyTyped(typedChar: Char, keyCode: Int) {
        // Processa apenas o frame no topo
        activeFrames.lastOrNull()?.let { frame ->
            if (frame.isVisible()) {
                frame.keyTyped(typedChar, keyCode)
            }
        }
    }
    
    /**
     * Atualiza todos os frames ativos.
     */
    fun updateFrames(partialTicks: Float) {
        activeFrames.forEach { frame ->
            if (frame.isVisible()) {
                frame.update(partialTicks)
            }
        }
    }
    
    /**
     * Retorna todos os frames ativos.
     */
    fun getActiveFrames(): List<Frame> = activeFrames.toList()
    
    /**
     * Retorna o frame no topo (último aberto).
     */
    fun getTopFrame(): Frame? = activeFrames.lastOrNull()

    /**
     * Verifica se há frames abertos.
     */
    @JvmStatic
    fun hasActiveFrames(): Boolean = activeFrames.isNotEmpty()
    
    /**
     * Remove frames que não estão mais visíveis.
     */
    fun cleanupInvisibleFrames() {
        activeFrames.removeAll { !it.isVisible() }
    }
}
