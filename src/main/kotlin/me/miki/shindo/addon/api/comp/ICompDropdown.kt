package me.miki.shindo.addon.api.comp

/**
 * Componente dropdown para addons. Lista de opções, uma selecionada.
 */
interface ICompDropdown : IComp {

    fun getOptions(): List<String>
    fun getSelectedIndex(): Int
    fun getSelected(): String?
    fun setSelectedIndex(index: Int)
}
