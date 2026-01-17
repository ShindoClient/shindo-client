package me.miki.shindo.ui.minecraft.component

import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import net.minecraft.client.gui.GuiSlider

/**
 * Renderizador customizado para sliders do Minecraft (GuiSlider).
 * Aplica o estilo visual do Shindo Client mantendo toda a funcionalidade original.
 */
object MinecraftSliderRenderer {
    
    /**
     * Renderiza um slider do Minecraft com o estilo do Shindo Client.
     * 
     * @param slider O slider a ser renderizado
     * @param mouseX Posição X do mouse
     * @param mouseY Posição Y do mouse
     * @param partialTicks Ticks parciais para animações
     */
    fun renderSlider(slider: GuiSlider, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!slider.visible) return
        
        val framework = MinecraftUIFramework
        val nvg = framework.getNanoVG()
        val x = slider.xPosition.toFloat()
        val y = slider.yPosition.toFloat()
        val width = slider.width.toFloat()
        val height = slider.height.toFloat()
        
        val hovered = slider.isMouseOver()
        val dragging = slider.dragging
        val enabled = slider.enabled
        
        // Calcula posição do knob baseado no valor
        val value = slider.sliderValue
        val knobX = x + (value * (width - 8f))
        
        // Cores
        val trackColor = framework.getBackgroundColor(me.miki.shindo.management.color.palette.ColorType.DARK, 200)
        val fillColor = framework.getAccentGradientColor1(255)
        val knobColor = if (hovered || dragging) {
            framework.getAccentGradientColor2(255)
        } else {
            framework.getAccentGradientColor1(255)
        }
        
        // Renderiza trilha
        val trackY = y + (height - 2f) / 2f
        nvg.drawRoundedRect(x, trackY, width, 2f, 1f, trackColor)
        
        // Renderiza preenchimento
        nvg.drawRoundedRect(x, trackY, knobX - x + 4f, 2f, 1f, fillColor)
        
        // Renderiza knob
        val knobSize = 8f
        val knobY = y + (height - knobSize) / 2f
        nvg.drawRoundedRect(knobX, knobY, knobSize, knobSize, knobSize / 2f, knobColor)
        
        // Renderiza texto se disponível
        val displayString = slider.displayString
        if (displayString.isNotEmpty()) {
            val font = me.miki.shindo.management.nanovg.font.Fonts.REGULAR
            val fontSize = 9f
            val textColor = framework.getDefaultFontColor(255)
            nvg.drawText(displayString, x, y - 12f, textColor, fontSize, font)
        }
    }
}
