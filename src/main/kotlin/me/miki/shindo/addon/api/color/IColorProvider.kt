package me.miki.shindo.addon.api.color

import me.miki.shindo.addon.api.render.AddonColor

/**
 * Acesso ao sistema de cores do client (accent, tema, palette).
 * Usa [AddonColor] para manter a API livre de dependências do client.
 */
interface IColorProvider {

    /**
     * Cor de destaque principal (color1 do accent atual).
     */
    fun getAccentColor(): AddonColor

    /**
     * Cor secundária do accent (color2).
     */
    fun getAccentColorSecondary(): AddonColor

    /**
     * Cor interpolada entre color1 e color2 (para gradientes).
     * @param index 0..15 para posição na gradiente
     */
    fun getAccentInterpolated(index: Int): AddonColor

    /**
     * ID do tema atual (0=Dark, 1=Light, etc).
     */
    fun getThemeId(): Int

    /**
     * Cor de fundo escuro do tema (para overlays, mod menu).
     */
    fun getThemeDarkBackground(): AddonColor

    /**
     * Cor de fundo normal do tema.
     */
    fun getThemeNormalBackground(): AddonColor

    /**
     * Cor de fonte escura do tema.
     */
    fun getThemeDarkFontColor(): AddonColor

    /**
     * Cor de fonte normal do tema.
     */
    fun getThemeNormalFontColor(): AddonColor
}
