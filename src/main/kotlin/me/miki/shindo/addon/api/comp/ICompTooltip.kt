package me.miki.shindo.addon.api.comp

/**
 * Componente de tooltip overlay (show/hide com animação).
 */
interface ICompTooltip : IComp {

    fun show()
    fun hide()
    fun getText(): String
    fun setText(text: String)
}
