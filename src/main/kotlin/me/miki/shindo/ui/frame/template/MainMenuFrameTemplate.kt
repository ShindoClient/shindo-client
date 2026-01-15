package me.miki.shindo.ui.frame.template

import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.ui.frame.Frame
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.Minecraft

/**
 * Template específico para frames do mainmenu.
 * Mantém compatibilidade com o sistema de scenes existente.
 */
object MainMenuFrameTemplate : FrameTemplate {
    override fun createFrame(parent: GuiScreen?, title: String): Frame {
        return object : Frame(parent) {
            override fun getTitle(): String = title
            
            override fun setupFrame() {
                val sr = ScaledResolution(Minecraft.getMinecraft())
                // Frame ocupa a maior parte da tela, mas não fullscreen
                width = (sr.scaledWidth * 0.85f).coerceAtMost(900f)
                height = (sr.scaledHeight * 0.85f).coerceAtMost(700f)
                x = (sr.scaledWidth - width) / 2f
                y = (sr.scaledHeight - height) / 2f
            }
        }.apply {
            init()
            applyTemplate(this)
        }
    }
    
    override fun applyTemplate(frame: Frame) {
        // Configurações específicas para mainmenu
        frame.setClosable(false) // Scenes do mainmenu não têm botão close
        frame.getContainer().setScrollable(true) // Suporte a scroll para conteúdo extenso
        frame.getContainer().setPadding(20f) // Padding generoso
    }
}

/**
 * Template para frames de welcome (primeiro login).
 * Frames menores e mais focados.
 */
object WelcomeFrameTemplate : FrameTemplate {
    override fun createFrame(parent: GuiScreen?, title: String): Frame {
        return object : Frame(parent) {
            override fun getTitle(): String = title
            
            override fun setupFrame() {
                val sr = ScaledResolution(Minecraft.getMinecraft())
                width = 600f
                height = 400f
                x = (sr.scaledWidth - width) / 2f
                y = (sr.scaledHeight - height) / 2f
            }
        }.apply {
            init()
            applyTemplate(this)
        }
    }
    
    override fun applyTemplate(frame: Frame) {
        frame.setClosable(false)
        frame.getContainer().setPadding(30f)
    }
}

/**
 * Template para frames de configuração (Background, Shop, Skin).
 * Frames médios com scroll.
 */
object ConfigFrameTemplate : FrameTemplate {
    override fun createFrame(parent: GuiScreen?, title: String): Frame {
        return object : Frame(parent) {
            override fun getTitle(): String = title
            
            override fun setupFrame() {
                val sr = ScaledResolution(Minecraft.getMinecraft())
                width = 700f
                height = 550f
                x = (sr.scaledWidth - width) / 2f
                y = (sr.scaledHeight - height) / 2f
            }
        }.apply {
            init()
            applyTemplate(this)
        }
    }
    
    override fun applyTemplate(frame: Frame) {
        frame.setClosable(false)
        frame.getContainer().setScrollable(true)
        frame.getContainer().setPadding(24f)
    }
}
