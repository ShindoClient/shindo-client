package me.miki.shindo.addon.api.comp

/**
 * Componente keybind para addons. Exibe e permite redefinir tecla.
 */
interface ICompKeybind : IComp {

    var keyCode: Int

    fun isBinding(): Boolean
}
