package me.miki.shindo.ui.minecraft.component

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import net.minecraft.client.gui.GuiResourcePackAvailable
import net.minecraft.client.gui.GuiResourcePackSelected
import java.awt.Color

/**
 * Renderizador customizado para listas de resource packs do Minecraft.
 * Aplica o estilo visual do Shindo Client mantendo toda a funcionalidade original.
 */
object MinecraftResourcePackListRenderer {
    
    /**
     * Renderiza uma lista de resource packs disponíveis.
     */
    fun renderAvailableList(list: GuiResourcePackAvailable, mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Reutiliza o renderizador de lista genérico
        MinecraftListRenderer.renderList(list, mouseX, mouseY, partialTicks)
    }
    
    /**
     * Renderiza uma lista de resource packs selecionados.
     */
    fun renderSelectedList(list: GuiResourcePackSelected, mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Reutiliza o renderizador de lista genérico
        MinecraftListRenderer.renderList(list, mouseX, mouseY, partialTicks)
    }
}
