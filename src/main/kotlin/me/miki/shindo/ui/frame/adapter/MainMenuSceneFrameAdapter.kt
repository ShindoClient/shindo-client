package me.miki.shindo.ui.frame.adapter

import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.ui.frame.Frame
import me.miki.shindo.ui.frame.template.FrameTemplate
import me.miki.shindo.ui.frame.template.MainMenuFrameTemplate
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.Minecraft

/**
 * Adapter que permite usar frames dentro de scenes do mainmenu
 * mantendo compatibilidade total com o sistema existente.
 * 
 * Uso:
 * ```kotlin
 * class MinhaScene(parent: GuiShindoMainMenu) : MainMenuScene(parent) {
 *     private val frameAdapter = MainMenuSceneFrameAdapter(this, MainMenuFrameTemplate, "Título")
 *     
 *     override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
 *         frameAdapter.draw(mouseX, mouseY, partialTicks)
 *     }
 *     
 *     override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
 *         frameAdapter.mouseClicked(mouseX, mouseY, mouseButton)
 *     }
 * }
 * ```
 */
class MainMenuSceneFrameAdapter(
    private val scene: MainMenuScene,
    template: FrameTemplate = MainMenuFrameTemplate,
    title: String = ""
) {
    private val frame: Frame = template.createFrame(scene.getParent(), title)
    
    init {
        // Garante que o frame está inicializado
        if (!::frame.isInitialized) {
            frame.init()
        }
    }
    
    /**
     * Retorna o frame interno para acesso direto.
     */
    fun getFrame(): Frame = frame
    
    /**
     * Renderiza o frame.
     * Chame este método no drawScreen() da scene.
     */
    fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvg = scene.mc.let { 
            me.miki.shindo.Shindo.getInstance().nanoVGManager 
        }
        
        nvg?.setupAndDraw {
            frame.draw(mouseX, mouseY, partialTicks)
        }
    }
    
    /**
     * Processa cliques do mouse.
     * Chame este método no mouseClicked() da scene.
     */
    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        frame.mouseClicked(mouseX, mouseY, mouseButton)
    }
    
    /**
     * Processa soltura do mouse.
     * Chame este método no mouseReleased() da scene.
     */
    fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        frame.mouseReleased(mouseX, mouseY, mouseButton)
    }
    
    /**
     * Processa teclas digitadas.
     * Chame este método no keyTyped() da scene.
     */
    fun keyTyped(typedChar: Char, keyCode: Int) {
        frame.keyTyped(typedChar, keyCode)
    }
    
    /**
     * Atualiza o frame.
     * Chame este método no update() da scene se necessário.
     */
    fun update(partialTicks: Float) {
        frame.update(partialTicks)
    }
    
    /**
     * Anexa um componente ao frame.
     * Método de conveniência que delega para frame.attachToFrame().
     */
    fun attachToFrame(component: me.miki.shindo.ui.comp.Comp) {
        frame.attachToFrame(component)
    }
    
    /**
     * Anexa múltiplos componentes ao frame.
     */
    fun attachToFrame(vararg components: me.miki.shindo.ui.comp.Comp) {
        frame.attachToFrame(*components)
    }
    
    /**
     * Acessa o container do frame para configurações avançadas.
     */
    fun getContainer() = frame.getContainer()
    
    /**
     * Acessa o header do frame para configurações avançadas.
     */
    fun getHeader() = frame.getHeader()
}
