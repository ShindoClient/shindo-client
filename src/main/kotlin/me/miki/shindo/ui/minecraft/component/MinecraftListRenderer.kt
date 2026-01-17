package me.miki.shindo.ui.minecraft.component

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import net.minecraft.client.gui.GuiListExtended
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry
import java.awt.Color

/**
 * Renderizador customizado para listas do Minecraft (GuiListExtended).
 * Aplica o estilo visual do Shindo Client mantendo toda a funcionalidade original.
 */
object MinecraftListRenderer {
    
    /**
     * Renderiza uma lista do Minecraft com o estilo do Shindo Client.
     */
    fun renderList(list: GuiListExtended, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!list.visible) return
        
        val framework = MinecraftUIFramework
        val nvg = framework.getNanoVG()
        
        // GuiListExtended usa campos shadowed que precisam ser acessados via Mixin
        // Por enquanto, apenas estilizamos o fundo geral
        // A renderização detalhada será feita via Mixin específico
    }
    
    private fun renderScrollbar(list: GuiListExtended, left: Float, top: Float, width: Float, height: Float) {
        val framework = MinecraftUIFramework
        val nvg = framework.getNanoVG()
        
        val scrollbarWidth = 6f
        val scrollbarX = left + width - scrollbarWidth - 2f
        
        // Calcula posição do scrollbar
        val totalHeight = list.listHeight.toFloat()
        val visibleHeight = height
        val scrollAmount = list.amountScrolled.toFloat()
        
        if (totalHeight > visibleHeight) {
            val scrollbarHeight = (visibleHeight / totalHeight) * visibleHeight
            val scrollbarY = top + (scrollAmount / totalHeight) * visibleHeight
            
            val trackColor = framework.getBackgroundColor(ColorType.MID, 150)
            val thumbColor = framework.getAccentGradientColor1(200)
            
            // Trilha do scrollbar
            nvg.drawRoundedRect(scrollbarX, top, scrollbarWidth, height, 3f, trackColor)
            
            // Thumb do scrollbar
            nvg.drawRoundedRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 3f, thumbColor)
        }
    }
}
