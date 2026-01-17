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
                val w = (sr.scaledWidth * 0.85f).coerceAtMost(900f)
                val h = (sr.scaledHeight * 0.85f).coerceAtMost(700f)
                setSize(w, h)
                setPosition((sr.scaledWidth - w) / 2f, (sr.scaledHeight - h) / 2f)
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
                val w = 600f
                val h = 400f
                setSize(w, h)
                setPosition((sr.scaledWidth - w) / 2f, (sr.scaledHeight - h) / 2f)
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
                val w = 700f
                val h = 550f
                setSize(w, h)
                setPosition((sr.scaledWidth - w) / 2f, (sr.scaledHeight - h) / 2f)
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
