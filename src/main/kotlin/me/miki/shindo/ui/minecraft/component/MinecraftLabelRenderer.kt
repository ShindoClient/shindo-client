package me.miki.shindo.ui.minecraft.component

import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import net.minecraft.client.gui.GuiLabel
import java.awt.Color

/**
 * Renderizador customizado para labels do Minecraft (GuiLabel).
 * Aplica o estilo visual do Shindo Client mantendo toda a funcionalidade original.
 */
object MinecraftLabelRenderer {
    
    /**
     * Renderiza um label do Minecraft com o estilo do Shindo Client.
     */
    fun renderLabel(label: GuiLabel, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!label.visible) return
        
        val framework = MinecraftUIFramework
        val nvg = framework.getNanoVG()
        val x = label.xPosition.toFloat()
        val y = label.yPosition.toFloat()
        
        val enabled = label.enabled
        val textColor = if (enabled) {
            framework.getDefaultFontColor(255)
        } else {
            framework.getFontColor(me.miki.shindo.management.color.palette.ColorType.NORMAL, 150)
        }
        
        val font = Fonts.REGULAR
        val fontSize = 10f
        
        // GuiLabel renderiza usando FontRenderer do Minecraft
        // Este renderizador apenas estiliza o container se necessário
        // O texto é renderizado pelo Minecraft usando FontRenderer
        // Por enquanto, não fazemos nada aqui pois o label usa renderização nativa
    }
}
