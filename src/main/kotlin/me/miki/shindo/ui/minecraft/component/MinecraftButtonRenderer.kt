package me.miki.shindo.ui.minecraft.component

import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import me.miki.shindo.utils.ColorUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * Renderizador customizado para botões do Minecraft (GuiButton).
 * Aplica o estilo visual do Shindo Client mantendo toda a funcionalidade original.
 */
object MinecraftButtonRenderer {
    
    /**
     * Renderiza um botão do Minecraft com o estilo do Shindo Client.
     * 
     * @param button O botão a ser renderizado
     * @param mouseX Posição X do mouse
     * @param mouseY Posição Y do mouse
     * @param partialTicks Ticks parciais para animações
     */
    fun renderButton(button: GuiButton, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!button.visible) return
        
        val framework = MinecraftUIFramework
        val nvg = framework.getNanoVG()
        
        val x = button.xPosition.toFloat()
        val y = button.yPosition.toFloat()
        val width = button.width.toFloat()
        val height = button.height.toFloat()
        
        val hovered = button.isMouseOver()
        val enabled = button.enabled
        
        // Cores baseadas no estado
        val backgroundColor = when {
            !enabled -> framework.getBackgroundColor(ColorType.DARK, 180)
            hovered -> framework.getHoverBackgroundColor(255)
            else -> framework.getDefaultBackgroundColor(255)
        }
        
        val textColor = when {
            !enabled -> framework.getFontColor(ColorType.NORMAL, 150)
            else -> framework.getDefaultFontColor(255)
        }
        
        // Renderiza fundo com bordas arredondadas
        val radius = 4f
        nvg.drawRoundedRect(x, y, width, height, radius, backgroundColor)
        
        // Renderiza gradiente de destaque se hovered e enabled
        if (hovered && enabled) {
            val accent1 = framework.getAccentGradientColor1(80)
            val accent2 = framework.getAccentGradientColor2(80)
            nvg.drawGradientRoundedRect(x, y, width, height, radius, accent1, accent2)
        }
        
        // Renderiza texto centralizado
        val text = button.displayString
        if (text.isNotEmpty()) {
            val font = Fonts.REGULAR
            val fontSize = 10f
            val textWidth = nvg.getTextWidth(text, fontSize, font)
            val textX = x + (width - textWidth) / 2f
            val textY = y + (height + 8f) / 2f // 8f é aproximadamente a altura da fonte
            
            nvg.drawText(text, textX, textY, textColor, fontSize, font)
        }
    }
    
    /**
     * Renderiza um botão usando o sistema de renderização do Minecraft (fallback).
     * Usado quando o framework está desabilitado ou para compatibilidade.
     */
    fun renderButtonVanilla(button: GuiButton, mouseX: Int, mouseY: Int) {
        // Deixa o Minecraft renderizar normalmente
        // Este método é apenas um placeholder para referência
    }
}
