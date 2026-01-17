package me.miki.shindo.ui.minecraft.screen

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import net.minecraft.client.gui.GuiControls
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

/**
 * Renderizador customizado para o menu de controles do Minecraft (GuiControls).
 * Aplica o estilo visual do Shindo Client mantendo toda a funcionalidade original.
 */
object MinecraftControlsScreenRenderer {
    
    /**
     * Renderiza o fundo e layout do menu de controles com o estilo do Shindo Client.
     */
    fun renderControlsScreen(screen: GuiControls, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val framework = MinecraftUIFramework
        val nvg = framework.getNanoVG()
        val mc = net.minecraft.client.Minecraft.getMinecraft()
        val sr = ScaledResolution(mc)
        
        val width = sr.scaledWidth.toFloat()
        val height = sr.scaledHeight.toFloat()
        
        // Renderiza fundo com gradiente sutil
        val bgColor1 = framework.getBackgroundColor(ColorType.DARK, 250)
        val bgColor2 = framework.getBackgroundColor(ColorType.MID, 200)
        nvg.drawVerticalGradientRect(0f, 0f, width, height, bgColor1, bgColor2)
        
        // Renderiza painel central
        val panelWidth = 500f
        val panelHeight = height - 100f
        val panelX = (width - panelWidth) / 2f
        val panelY = 60f
        
        val panelBg = framework.getBackgroundColor(ColorType.NORMAL, 180)
        nvg.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 12f, panelBg)
        
        // Renderiza borda do painel
        val borderColor = framework.getFontColor(ColorType.NORMAL, 80)
        nvg.drawOutlineRoundedRect(panelX, panelY, panelWidth, panelHeight, 12f, 1f, borderColor)
        
        // Renderiza título com estilo
        val title = net.minecraft.client.resources.I18n.format("controls.title")
        val titleFont = Fonts.MEDIUM
        val titleSize = 24f
        val titleColor = framework.getDefaultFontColor(255)
        val titleY = 30f
        
        // Sombra do título
        val shadowColor = me.miki.shindo.utils.ColorUtils.applyAlpha(java.awt.Color.BLACK, 100)
        nvg.drawCenteredText(title, width / 2f + 2f, titleY + 2f, shadowColor, titleSize, titleFont)
        
        // Título principal
        nvg.drawCenteredText(title, width / 2f, titleY, titleColor, titleSize, titleFont)
        
        // Renderiza linha decorativa abaixo do título
        val lineY = titleY + 35f
        val lineWidth = 200f
        val lineX = (width - lineWidth) / 2f
        val accent1 = framework.getAccentGradientColor1(200)
        val accent2 = framework.getAccentGradientColor2(200)
        nvg.drawHorizontalGradientRect(lineX, lineY, lineWidth, 2f, accent1, accent2)
    }
}
