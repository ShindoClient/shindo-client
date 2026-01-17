package me.miki.shindo.ui.minecraft.component

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import java.awt.Color

/**
 * Renderizador customizado para slots de inventário do Minecraft.
 * Aplica o estilo visual do Shindo Client mantendo toda a funcionalidade original.
 */
object MinecraftSlotRenderer {
    
    /**
     * Renderiza um slot de inventário com o estilo do Shindo Client.
     */
    fun renderSlot(container: GuiContainer, slot: Slot, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val framework = MinecraftUIFramework
        val nvg = framework.getNanoVG()
        
        val x = slot.xDisplayPosition.toFloat()
        val y = slot.yDisplayPosition.toFloat()
        val size = 16f
        
        val hovered = isSlotHovered(container, slot, mouseX, mouseY)
        
        // Renderiza fundo do slot apenas quando hovered
        if (hovered) {
            val bgColor = framework.getHoverBackgroundColor(180)
            nvg.drawRoundedRect(x - 1f, y - 1f, size + 2f, size + 2f, 2f, bgColor)
            
            // Renderiza borda destacada
            val borderColor = framework.getAccentGradientColor1(255)
            nvg.drawOutlineRoundedRect(x - 1f, y - 1f, size + 2f, size + 2f, 2f, 1f, borderColor)
        }
    }
    
    private fun isSlotHovered(container: GuiContainer, slot: Slot, mouseX: Int, mouseY: Int): Boolean {
        val x = slot.xDisplayPosition
        val y = slot.yDisplayPosition
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16
    }
}
