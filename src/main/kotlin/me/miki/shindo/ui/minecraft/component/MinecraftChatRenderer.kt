package me.miki.shindo.ui.minecraft.component

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import net.minecraft.client.gui.GuiChat
import java.awt.Color

/**
 * Renderizador customizado para o chat do Minecraft (GuiChat).
 * Aplica o estilo visual do Shindo Client mantendo toda a funcionalidade original.
 */
object MinecraftChatRenderer {
    
    /**
     * Renderiza o fundo do chat com o estilo do Shindo Client.
     */
    fun renderChatBackground(chat: GuiChat, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val framework = MinecraftUIFramework
        val nvg = framework.getNanoVG()
        
        val width = chat.width.toFloat()
        val height = chat.height.toFloat()
        
        // Renderiza fundo do chat com transparência
        val bgColor = framework.getBackgroundColor(ColorType.DARK, 180)
        nvg.drawRoundedRect(0f, height - 14f, width, 14f, 4f, bgColor)
    }
}
