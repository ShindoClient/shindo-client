package me.miki.shindo.ui.frame.template

import me.miki.shindo.ui.frame.Frame
import net.minecraft.client.gui.GuiScreen

/**
 * Interface base para templates de frame.
 * Templates facilitam a criação de frames com configurações pré-definidas.
 */
interface FrameTemplate {
    /**
     * Cria um frame usando este template.
     */
    fun createFrame(parent: GuiScreen? = null, title: String = ""): Frame
    
    /**
     * Aplica configurações do template ao frame.
     */
    fun applyTemplate(frame: Frame)
}

/**
 * Template padrão para frames.
 * Fornece configurações básicas e comuns.
 */
object StandardFrameTemplate : FrameTemplate {
    override fun createFrame(parent: GuiScreen?, title: String): Frame {
        return object : Frame(parent) {
            override fun getTitle(): String = title
        }.apply {
            init()
            applyTemplate(this)
        }
    }
    
    override fun applyTemplate(frame: Frame) {
        // Configurações padrão já aplicadas no init()
        // Pode ser estendido para adicionar mais configurações
    }
}

/**
 * Template para frames pequenos (dialogs, confirmações).
 */
object SmallFrameTemplate : FrameTemplate {
    override fun createFrame(parent: GuiScreen?, title: String): Frame {
        return object : Frame(parent) {
            override fun getTitle(): String = title
            
            override fun setupFrame() {
                super.setupFrame()
                width = 320f
                height = 180f
            }
        }.apply {
            init()
            applyTemplate(this)
        }
    }
    
    override fun applyTemplate(frame: Frame) {
        // Já configurado no setupFrame
    }
}

/**
 * Template para frames médios (formulários, configurações).
 */
object MediumFrameTemplate : FrameTemplate {
    override fun createFrame(parent: GuiScreen?, title: String): Frame {
        return object : Frame(parent) {
            override fun getTitle(): String = title
            
            override fun setupFrame() {
                super.setupFrame()
                width = 500f
                height = 400f
            }
        }.apply {
            init()
            applyTemplate(this)
        }
    }
    
    override fun applyTemplate(frame: Frame) {
        // Já configurado no setupFrame
    }
}

/**
 * Template para frames grandes (listas, tabelas, conteúdo extenso).
 */
object LargeFrameTemplate : FrameTemplate {
    override fun createFrame(parent: GuiScreen?, title: String): Frame {
        return object : Frame(parent) {
            override fun getTitle(): String = title
            
            override fun setupFrame() {
                super.setupFrame()
                val sr = net.minecraft.client.gui.ScaledResolution(
                    net.minecraft.client.Minecraft.getMinecraft()
                )
                width = (sr.scaledWidth * 0.8f).coerceAtMost(800f)
                height = (sr.scaledHeight * 0.8f).coerceAtMost(600f)
            }
        }.apply {
            init()
            applyTemplate(this)
        }
    }
    
    override fun applyTemplate(frame: Frame) {
        // Já configurado no setupFrame
        frame.getContainer().setScrollable(true)
    }
}

/**
 * Template para frames fullscreen (modais grandes).
 */
object FullscreenFrameTemplate : FrameTemplate {
    override fun createFrame(parent: GuiScreen?, title: String): Frame {
        return object : Frame(parent) {
            override fun getTitle(): String = title
            
            override fun setupFrame() {
                val sr = net.minecraft.client.gui.ScaledResolution(
                    net.minecraft.client.Minecraft.getMinecraft()
                )
                x = 0f
                y = 0f
                width = sr.scaledWidth.toFloat()
                height = sr.scaledHeight.toFloat()
            }
        }.apply {
            init()
            applyTemplate(this)
        }
    }
    
    override fun applyTemplate(frame: Frame) {
        frame.setClosable(true)
    }
}
