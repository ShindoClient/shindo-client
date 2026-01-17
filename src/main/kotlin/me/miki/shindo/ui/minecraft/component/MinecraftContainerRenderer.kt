package me.miki.shindo.ui.minecraft.component

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import net.minecraft.client.gui.inventory.GuiContainer
import java.awt.Color

/**
 * Renderizador customizado para containers do Minecraft (GuiContainer).
 * Aplica o estilo visual do Shindo Client mantendo toda a funcionalidade original.
 */
object MinecraftContainerRenderer {
    
    /**
     * Renderiza o fundo de um container com o estilo do Shindo Client.
     */
    fun renderContainerBackground(container: GuiContainer, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val framework = MinecraftUIFramework
        val nvg = framework.getNanoVG()
        
        val xSize = container.xSize.toFloat()
        val ySize = container.ySize.toFloat()
        val guiLeft = container.guiLeft.toFloat()
        val guiTop = container.guiTop.toFloat()
        
        // Renderiza fundo do container com transparência
        val bgColor = framework.getBackgroundColor(ColorType.DARK, 240)
        nvg.drawRoundedRect(guiLeft, guiTop, xSize, ySize, 8f, bgColor)
        
        // Renderiza borda
        val borderColor = framework.getFontColor(ColorType.NORMAL, 150)
        nvg.drawOutlineRoundedRect(guiLeft, guiTop, xSize, ySize, 8f, 2f, borderColor)
    }
}
