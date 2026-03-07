package me.miki.shindo.addon.api.comp

/**
 * Componente com área scrollável. Retornado por createScrollable.
 * Defina contentHeight e adicione filhos via addChild.
 */
interface IScrollableComp : IComp {

    fun setContentHeight(height: Float)

    fun getScrollY(): Float

    fun setScrollY(value: Float)

    fun scrollBy(delta: Float)
}
