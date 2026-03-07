package me.miki.shindo.addon.api.comp

/**
 * Componente slider para addons. Valor numérico com min/max.
 */
interface ICompSlider : IComp {

    var value: Double

    fun getMin(): Double
    fun getMax(): Double
    fun getStep(): Double
    fun isInteger(): Boolean
}
