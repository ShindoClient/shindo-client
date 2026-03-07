package me.miki.shindo.addon.api.comp

/**
 * Componente barra de progresso. Retornado por createProgressBar.
 */
interface ICompProgressBar : IComp {

    var progress: Float
    var maxProgress: Float
}
