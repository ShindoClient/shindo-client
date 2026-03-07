package me.miki.shindo.addon.api.comp

import me.miki.shindo.addon.api.render.AddonColor

/**
 * Componente color picker para addons.
 */
interface ICompColorPicker : IComp {

    var color: AddonColor

    fun isOpen(): Boolean
}
