package me.miki.shindo.ui.minecraft

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.utils.ColorUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import java.awt.Color

/**
 * Framework principal para redesenhar UIs do Minecraft com o estilo do Shindo Client.
 * 
 * Este framework permite:
 * - Aplicar o estilo visual do Shindo Client a todas as UIs do Minecraft
 * - Manter toda a funcionalidade original (botões, campos de texto, etc.)
 * - Personalizar cores, temas e estilos
 * - Interceptar e redesenhar componentes do Minecraft
 */
object MinecraftUIFramework {
    
    private val mc: Minecraft = Minecraft.getMinecraft()
    
    /**
     * Verifica se o framework está habilitado.
     */
    var enabled: Boolean = true
        private set
    
    /**
     * Habilita ou desabilita o framework.
     */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
    
    /**
     * Obtém o gerenciador de cores do Shindo Client.
     */
    fun getColorManager(): ColorManager {
        return Shindo.getInstance().colorManager
    }
    
    /**
     * Obtém a paleta de cores atual.
     */
    fun getPalette(): ColorPalette {
        return getColorManager().palette
    }
    
    /**
     * Obtém a cor de destaque atual.
     */
    fun getAccentColor(): AccentColor {
        return getColorManager().currentColor
    }
    
    /**
     * Obtém o gerenciador NanoVG.
     */
    fun getNanoVG(): NanoVGManager {
        return Shindo.getInstance().nanoVGManager!!
    }
    
    /**
     * Obtém a cor de fundo baseada no tipo.
     */
    fun getBackgroundColor(type: ColorType, alpha: Int = 255): Color {
        return getPalette().getBackgroundColor(type, alpha)
    }
    
    /**
     * Obtém a cor de texto baseada no tipo.
     */
    fun getFontColor(type: ColorType, alpha: Int = 255): Color {
        return getPalette().getFontColor(type, alpha)
    }
    
    /**
     * Obtém a cor de fundo padrão para componentes do Minecraft.
     */
    fun getDefaultBackgroundColor(alpha: Int = 255): Color {
        return getBackgroundColor(ColorType.NORMAL, alpha)
    }
    
    /**
     * Obtém a cor de texto padrão para componentes do Minecraft.
     */
    fun getDefaultFontColor(alpha: Int = 255): Color {
        return getFontColor(ColorType.NORMAL, alpha)
    }
    
    /**
     * Obtém a cor de fundo quando hover.
     */
    fun getHoverBackgroundColor(alpha: Int = 255): Color {
        return getBackgroundColor(ColorType.MID, alpha)
    }
    
    /**
     * Obtém a cor de destaque (accent) para botões ativos.
     */
    fun getAccentGradientColor1(alpha: Int = 255): Color {
        return ColorUtils.applyAlpha(getAccentColor().color1, alpha)
    }
    
    /**
     * Obtém a segunda cor de destaque (accent) para gradientes.
     */
    fun getAccentGradientColor2(alpha: Int = 255): Color {
        return ColorUtils.applyAlpha(getAccentColor().color2, alpha)
    }
    
    /**
     * Verifica se uma tela deve ter o estilo do Shindo aplicado.
     * Por padrão, aplica a todas as telas, exceto telas do Shindo Client (que já têm estilo próprio).
     */
    fun shouldApplyStyle(screen: GuiScreen?): Boolean {
        if (!enabled) return false
        if (screen == null) return false
        
        // Não aplica em telas do Shindo Client (elas já têm estilo próprio)
        return screen !is me.miki.shindo.gui.IShindoScreen
    }
}
