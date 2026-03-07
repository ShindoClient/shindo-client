package me.miki.shindo.addon.api.comp

/**
 * Componente de badge (texto com fundo arredondado).
 */
interface ICompBadge : IComp {

    fun getText(): String
    fun setText(text: String)
}
