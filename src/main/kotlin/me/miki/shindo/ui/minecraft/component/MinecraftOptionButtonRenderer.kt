package me.miki.shindo.ui.minecraft.component

import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import net.minecraft.client.gui.GuiOptionButton
import java.awt.Color

/**
 * Renderizador customizado para botões de opção do Minecraft (GuiOptionButton).
 * Similar ao GuiButton mas com suporte a opções de configuração.
 */
object MinecraftOptionButtonRenderer {
    
    /**
     * Renderiza um botão de opção do Minecraft com o estilo do Shindo Client.
     */
    fun renderOptionButton(button: GuiOptionButton, mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Reutiliza o renderizador de botão padrão
        MinecraftButtonRenderer.renderButton(button, mouseX, mouseY, partialTicks)
    }
}
