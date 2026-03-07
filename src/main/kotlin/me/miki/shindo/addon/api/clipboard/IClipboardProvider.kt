package me.miki.shindo.addon.api.clipboard

/**
 * Acesso ao clipboard do sistema.
 */
interface IClipboardProvider {

    /** Copia texto para o clipboard. */
    fun setText(text: String)

    /** Obtém texto do clipboard, ou null se não for texto. */
    fun getText(): String?
}
