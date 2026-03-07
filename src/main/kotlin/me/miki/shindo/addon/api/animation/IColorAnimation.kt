package me.miki.shindo.addon.api.animation

import me.miki.shindo.addon.api.render.AddonColor

/**
 * Interface de animação de cor. Interpola suavemente entre cores.
 * O client fornece implementação (ex: ColorAnimation).
 */
interface IColorAnimation {

    fun getColor(color: AddonColor): AddonColor

    fun getColor(color: AddonColor, speed: Int): AddonColor

    fun setColor(color: AddonColor)
}
