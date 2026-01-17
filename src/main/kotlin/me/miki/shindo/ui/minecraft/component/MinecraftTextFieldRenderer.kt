package me.miki.shindo.ui.minecraft.component

import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import me.miki.shindo.utils.ColorUtils
import net.minecraft.client.gui.GuiTextField
import java.awt.Color

/**
 * Renderizador customizado para campos de texto do Minecraft (GuiTextField).
 * Aplica o estilo visual do Shindo Client mantendo toda a funcionalidade original.
 */
object MinecraftTextFieldRenderer {
    
    /**
     * Renderiza um campo de texto do Minecraft com o estilo do Shindo Client.
     * 
     * @param textField O campo de texto a ser renderizado
     * @param mouseX Posição X do mouse
     * @param mouseY Posição Y do mouse
     * @param partialTicks Ticks parciais para animações
     */
    fun renderTextField(textField: GuiTextField, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!textField.visible) return
        
        val framework = MinecraftUIFramework
        val nvg = framework.getNanoVG()
        
        val x = textField.xPosition.toFloat()
        val y = textField.yPosition.toFloat()
        val width = textField.width.toFloat()
        val height = textField.height.toFloat()
        
        val focused = textField.isFocused
        val enabled = textField.isEnabled
        
        // Cores baseadas no estado
        val backgroundColor = when {
            !enabled -> framework.getBackgroundColor(ColorType.DARK, 200)
            focused -> framework.getBackgroundColor(ColorType.MID, 255)
            else -> framework.getDefaultBackgroundColor(255)
        }
        
        val borderColor = when {
            !enabled -> framework.getFontColor(ColorType.NORMAL, 100)
            focused -> framework.getAccentGradientColor1(255)
            else -> framework.getFontColor(ColorType.NORMAL, 150)
        }
        
        val textColor = when {
            !enabled -> framework.getFontColor(ColorType.NORMAL, 150)
            else -> framework.getDefaultFontColor(255)
        }
        
        // Renderiza fundo
        val radius = 4f
        nvg.drawRoundedRect(x, y, width, height, radius, backgroundColor)
        
        // Renderiza borda
        val borderWidth = if (focused) 2f else 1f
        nvg.drawOutlineRoundedRect(x, y, width, height, radius, borderWidth, borderColor)
        
        // Renderiza texto
        val text = textField.text
        val font = me.miki.shindo.management.nanovg.font.Fonts.REGULAR
        val fontSize = 10f
        val textX = x + 4f
        val textY = y + (height + 8f) / 2f
        
        // Renderiza texto com cursor se focado
        if (text.isNotEmpty()) {
            nvg.drawText(text, textX, textY, textColor, fontSize, font)
        }
        
        // Renderiza cursor se focado
        if (focused && (System.currentTimeMillis() / 500) % 2 == 0L) {
            val cursorX = textX + (if (text.isNotEmpty()) nvg.getTextWidth(text, fontSize, font) else 0f)
            nvg.drawRect(cursorX, textY - 1f, 1f, 10f, textColor)
        }
    }
}
