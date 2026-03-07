package me.miki.shindo.addon.api.comp

/**
 * Componente de caixa de texto. Retornado por createTextBox.
 * Estende IComp com getText/setText.
 */
interface ICompTextBox : IComp {

    fun getText(): String

    fun setText(text: String)

    fun setDefaultText(text: String?)

    fun setMaxLength(max: Int)

    fun isFocused(): Boolean
}
