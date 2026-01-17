package me.miki.shindo.ui.minecraft.screen

import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import net.minecraft.client.gui.*

/**
 * Registro centralizado de renderizadores de telas do Minecraft.
 * Gerencia a renderização de telas específicas com o estilo do Shindo Client.
 */
object MinecraftScreenRegistry {
    
    /**
     * Renderiza uma tela do Minecraft com o estilo do Shindo Client.
     */
    fun renderScreen(screen: GuiScreen, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!MinecraftUIFramework.shouldApplyStyle(screen)) {
            return
        }
        
        when (screen) {
            is GuiOptions -> {
                MinecraftOptionsScreenRenderer.renderOptionsScreen(screen, mouseX, mouseY, partialTicks)
            }
            is GuiVideoSettings -> {
                MinecraftVideoSettingsScreenRenderer.renderVideoSettingsScreen(screen, mouseX, mouseY, partialTicks)
            }
            is GuiControls -> {
                MinecraftControlsScreenRenderer.renderControlsScreen(screen, mouseX, mouseY, partialTicks)
            }
            is GuiLanguage -> {
                // GuiLanguage já tem Mixin, apenas renderiza fundo se necessário
                renderGenericScreen(screen, mouseX, mouseY, partialTicks)
            }
            is GuiScreenResourcePacks -> {
                // GuiScreenResourcePacks já tem Mixin, apenas renderiza fundo se necessário
                renderGenericScreen(screen, mouseX, mouseY, partialTicks)
            }
            else -> {
                // Verifica se é uma tela do OptiFine
                if (MinecraftOptiFineScreenRenderer.isOptiFineScreen(screen)) {
                    MinecraftOptiFineScreenRenderer.renderOptiFineScreen(screen, mouseX, mouseY, partialTicks)
                } else {
                    // Renderiza fundo genérico para outras telas
                    renderGenericScreen(screen, mouseX, mouseY, partialTicks)
                }
            }
        }
    }
    
    /**
     * Renderiza um fundo genérico para telas que não têm renderizador específico.
     */
    private fun renderGenericScreen(screen: GuiScreen, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val framework = MinecraftUIFramework
        val nvg = framework.getNanoVG()
        val mc = net.minecraft.client.Minecraft.getMinecraft()
        val sr = net.minecraft.client.gui.ScaledResolution(mc)
        
        val width = sr.scaledWidth.toFloat()
        val height = sr.scaledHeight.toFloat()
        
        // Renderiza fundo com transparência
        val bgColor = framework.getBackgroundColor(me.miki.shindo.management.color.palette.ColorType.DARK, 240)
        nvg.drawRect(0f, 0f, width, height, bgColor)
    }
}
