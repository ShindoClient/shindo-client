package me.miki.shindo.addon.api.comp

/**
 * Componente toggle. Retornado por createToggleButton.
 */
interface IToggleComp : IComp {

    /** Valor do toggle (true = ligado, false = desligado). */
    var value: Boolean
}
