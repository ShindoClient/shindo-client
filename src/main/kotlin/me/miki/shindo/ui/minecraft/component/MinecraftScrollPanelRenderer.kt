package me.miki.shindo.ui.minecraft.component

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import net.minecraft.client.gui.GuiScreen
import java.awt.Color

/**
 * Renderizador customizado para painéis com scroll do Minecraft.
 * Aplica o estilo visual do Shindo Client mantendo toda a funcionalidade original.
 */
object MinecraftScrollPanelRenderer {
    
    /**
     * Renderiza um painel com scroll com o estilo do Shindo Client.
     */
    fun renderScrollPanel(screen: GuiScreen, x: Int, y: Int, width: Int, height: Int, scrollAmount: Float, maxScroll: Float) {
        val framework = MinecraftUIFramework
        val nvg = framework.getNanoVG()
        
        val xf = x.toFloat()
        val yf = y.toFloat()
        val widthf = width.toFloat()
        val heightf = height.toFloat()
        
        // Renderiza fundo do painel
        val bgColor = framework.getBackgroundColor(ColorType.DARK, 200)
        nvg.drawRoundedRect(xf, yf, widthf, heightf, 4f, bgColor)
        
        // Renderiza scrollbar se necessário
        if (maxScroll > 0) {
            renderScrollbar(nvg, framework, xf, yf, widthf, heightf, scrollAmount, maxScroll)
        }
    }
    
    private fun renderScrollbar(
        nvg: me.miki.shindo.management.nanovg.NanoVGManager,
        framework: MinecraftUIFramework,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        scrollAmount: Float,
        maxScroll: Float
    ) {
        val scrollbarWidth = 6f
        val scrollbarX = x + width - scrollbarWidth - 2f
        
        val scrollbarHeight = (height / (height + maxScroll)) * height
        val scrollbarY = y + (scrollAmount / maxScroll) * (height - scrollbarHeight)
        
        val trackColor = framework.getBackgroundColor(ColorType.MID, 150)
        val thumbColor = framework.getAccentGradientColor1(200)
        
        // Trilha do scrollbar
        nvg.drawRoundedRect(scrollbarX, y, scrollbarWidth, height, 3f, trackColor)
        
        // Thumb do scrollbar
        nvg.drawRoundedRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 3f, thumbColor)
    }
}
