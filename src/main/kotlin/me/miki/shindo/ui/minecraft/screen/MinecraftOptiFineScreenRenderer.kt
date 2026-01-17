package me.miki.shindo.ui.minecraft.screen

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.minecraft.MinecraftUIFramework
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

/**
 * Renderizador customizado para telas do OptiFine.
 * Aplica o estilo visual do Shindo Client mantendo toda a funcionalidade original do OptiFine.
 */
object MinecraftOptiFineScreenRenderer {
    
    /**
     * Renderiza uma tela do OptiFine com o estilo do Shindo Client.
     * Suporta:
     * - GuiVideoSettingsOF (Video Settings do OptiFine)
     * - GuiOtherSettingsOF (Other Settings do OptiFine)
     * - GuiQualitySettingsOF (Quality Settings do OptiFine)
     * - GuiPerformanceSettingsOF (Performance Settings do OptiFine)
     * - E outras telas do OptiFine
     */
    fun renderOptiFineScreen(screen: GuiScreen, mouseX: Int, mouseY: Int, partialTicks: Float) {
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
        
        // Tenta obter o título da tela
        val screenName = screen.javaClass.simpleName
        val title = getOptiFineScreenTitle(screenName)
        
        if (title != null) {
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
    
    /**
     * Obtém o título apropriado para uma tela do OptiFine baseado no nome da classe.
     */
    private fun getOptiFineScreenTitle(screenName: String): String? {
        return when {
            screenName.contains("VideoSettings", ignoreCase = true) -> 
                net.minecraft.client.resources.I18n.format("options.video")
            screenName.contains("OtherSettings", ignoreCase = true) -> 
                "Other Settings"
            screenName.contains("QualitySettings", ignoreCase = true) -> 
                "Quality Settings"
            screenName.contains("PerformanceSettings", ignoreCase = true) -> 
                "Performance Settings"
            screenName.contains("AnimationsSettings", ignoreCase = true) -> 
                "Animations Settings"
            screenName.contains("DetailsSettings", ignoreCase = true) -> 
                "Details Settings"
            else -> null
        }
    }
    
    /**
     * Verifica se uma tela é do OptiFine.
     */
    fun isOptiFineScreen(screen: GuiScreen): Boolean {
        val className = screen.javaClass.name
        val simpleName = screen.javaClass.simpleName
        
        return className.contains("optifine", ignoreCase = true) ||
               simpleName.contains("OF", ignoreCase = true) ||
               simpleName.contains("OptiFine", ignoreCase = true)
    }
}
